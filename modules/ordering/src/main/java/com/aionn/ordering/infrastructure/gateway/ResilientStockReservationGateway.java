package com.aionn.ordering.infrastructure.gateway;

import com.aionn.ordering.application.port.out.StockReservationGateway;
import com.aionn.ordering.application.port.out.observability.OrderingMetricsPort;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Component
@Primary
@Order(0)
public class ResilientStockReservationGateway implements StockReservationGateway {

    private static final String INSTANCE = "ordering-inventory";

    private final StockReservationGateway delegate;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;
    private final OrderingMetricsPort metrics;

    public ResilientStockReservationGateway(
            List<StockReservationGateway> delegates,
            RetryRegistry retryRegistry,
            CircuitBreakerRegistry circuitBreakerRegistry,
            OrderingMetricsPort metrics) {
        this.delegate = delegates.stream()
                .filter(impl -> !(impl instanceof ResilientStockReservationGateway))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No underlying StockReservationGateway implementation found"));
        this.retry = retryRegistry.retry(INSTANCE);
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(INSTANCE);
        this.metrics = metrics;
    }

    @Override
    public List<Reservation> reserveAll(String orderId, List<ReservationLine> lines, int ttlSeconds) {
        Supplier<List<Reservation>> action = () -> delegate.reserveAll(orderId, lines, ttlSeconds);
        try {
            List<Reservation> result = Retry.decorateSupplier(retry,
                    CircuitBreaker.decorateSupplier(circuitBreaker, action)).get();
            metrics.gatewayOutcome("inventory", "success");
            return result;
        } catch (ReservationException re) {
            metrics.gatewayOutcome("inventory", "reservation_failed");
            throw re;
        } catch (RuntimeException ex) {
            metrics.gatewayOutcome("inventory", "failure");
            log.error("Inventory reservation gateway failed: {}", ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void commit(String reservationId) {
        execute("commit", () -> {
            delegate.commit(reservationId);
            return null;
        });
    }

    @Override
    public void release(String reservationId, String reason) {
        execute("release", () -> {
            delegate.release(reservationId, reason);
            return null;
        });
    }

    private <T> T execute(String operation, Supplier<T> action) {
        Supplier<T> decorated = Retry.decorateSupplier(retry,
                CircuitBreaker.decorateSupplier(circuitBreaker, action));
        try {
            T result = decorated.get();
            metrics.gatewayOutcome("inventory", "success");
            return result;
        } catch (RuntimeException ex) {
            metrics.gatewayOutcome("inventory", "failure");
            log.error("Inventory gateway {} failed: {}", operation, ex.getMessage());
            throw ex;
        }
    }
}
