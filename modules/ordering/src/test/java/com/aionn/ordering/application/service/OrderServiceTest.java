package com.aionn.ordering.application.service;

import com.aionn.ordering.application.dto.order.command.CancelOrderCommand;
import com.aionn.ordering.application.dto.order.command.ConfirmDeliveredCommand;
import com.aionn.ordering.application.dto.order.command.ConfirmShippedCommand;
import com.aionn.ordering.application.dto.order.command.RejectOrderCommand;
import com.aionn.ordering.application.dto.order.result.OrderResult;
import com.aionn.ordering.application.mapper.OrderingResultMapper;
import com.aionn.ordering.application.port.out.CartPersistencePort;
import com.aionn.ordering.application.port.out.CatalogPricingGateway;
import com.aionn.ordering.application.port.out.OrderPersistencePort;
import com.aionn.ordering.application.port.out.PaymentGateway;
import com.aionn.ordering.application.port.out.ShippingGateway;
import com.aionn.ordering.application.port.out.StockReservationGateway;
import com.aionn.ordering.application.port.out.VoucherGateway;
import com.aionn.ordering.application.port.out.integration.OrderingIntegrationEventPublisherPort;
import com.aionn.ordering.domain.exception.OrderingException;
import com.aionn.ordering.domain.model.Order;
import com.aionn.ordering.domain.model.OrderItem;
import com.aionn.ordering.domain.valueobject.OrderStatus;
import com.aionn.ordering.domain.valueobject.ShippingAddress;
import com.aionn.ordering.infrastructure.config.OrderingProperties;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.sharedkernel.domain.vo.Money;
import com.aionn.sharedkernel.integration.port.catalog.MerchantQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final String USER_ID = "user-1";
    private static final String MERCHANT_ID = "merchant-1";
    private static final String ORDER_ID = "order-1";

    @Mock private CartPersistencePort cartRepository;
    @Mock private OrderPersistencePort orderRepository;
    @Mock private OrderingResultMapper mapper;
    @Mock private EventPublisher eventPublisher;
    @Mock private StockReservationGateway stockReservationGateway;
    @Mock private PaymentGateway paymentGateway;
    @Mock private ShippingGateway shippingGateway;
    @Mock private CatalogPricingGateway catalogPricingGateway;
    @Mock private VoucherGateway voucherGateway;
    @Mock private CartService cartService;
    @Mock private MerchantQueryPort merchantQueryPort;
    @Mock private OrderingIntegrationEventPublisherPort integrationEventPublisher;
    private final OrderingProperties orderingProperties = new OrderingProperties(
            new OrderingProperties.Reservation(86400),
            new OrderingProperties.AutoCancel(true, 15, 60_000L, 100));

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                cartRepository, orderRepository, mapper, eventPublisher,
                stockReservationGateway, paymentGateway, shippingGateway,
                catalogPricingGateway, voucherGateway, cartService, merchantQueryPort,
                integrationEventPublisher, orderingProperties);
    }

    private static ShippingAddress address() {
        return new ShippingAddress("addr-1", "User", "+84912345678",
                "12 main", "WARD", "DIST", "PROV", "VN");
    }

    private static OrderItem item() {
        return new OrderItem("sku-1", 2, Money.of(BigDecimal.valueOf(100), "VND"),
                "wh-1", null);
    }

    private static Order pendingOrder() {
        Money subtotal = Money.of(BigDecimal.valueOf(200), "VND");
        Money shipping = Money.of(BigDecimal.ZERO, "VND");
        return Order.place(ORDER_ID, USER_ID, MERCHANT_ID, "prop-1",
                "COD", "VND", List.of(item()), address(), shipping, subtotal);
    }

    private static OrderResult sampleResult(String status) {
        return new OrderResult(ORDER_ID, null, USER_ID, MERCHANT_ID, "prop-1",
                "COD", null, "VND", BigDecimal.valueOf(200),
                BigDecimal.ZERO, "addr-1", List.of(), status, null,
                java.time.Instant.now(), java.time.Instant.now(), null, null);
    }

    @Test
    void cancelMovesOrderToCancelledAndReleasesNothingWhenNoReservations() {
        Order order = pendingOrder();
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(mapper.toResult(order)).thenReturn(sampleResult("CANCELLED"));

        OrderResult result = orderService.cancel(new CancelOrderCommand(ORDER_ID, USER_ID, "changed"));

        assertEquals("CANCELLED", result.status());
        verify(integrationEventPublisher).publishOrderCancelled(eq(ORDER_ID),
                eq("USER_CANCELLED"), eq("changed"),
                eq(OrderingIntegrationEventPublisherPort.CancellationKind.USER_CANCELLED));
    }

    @Test
    void cancelRejectsForeignUser() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(pendingOrder()));

        assertThrows(OrderingException.class,
                () -> orderService.cancel(new CancelOrderCommand(ORDER_ID, "other-user", "x")));
    }

    @Test
    void cancelOnUnknownOrderThrows() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThrows(OrderingException.class,
                () -> orderService.cancel(new CancelOrderCommand(ORDER_ID, USER_ID, "n")));
    }

    @Test
    void completeRequiresShippedStatus() {
        Order order = pendingOrder();
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(OrderingException.class,
                () -> orderService.complete(new ConfirmDeliveredCommand(ORDER_ID)));
        verify(integrationEventPublisher, never()).publishOrderCompleted(anyString());
    }

    @Test
    void completeOnShippedOrderPublishesCompletedEvent() {
        Order order = pendingOrder();
        order.approve("p");
        order.confirmPreparation();
        order.markShipped("ship-1");
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(mapper.toResult(order)).thenReturn(sampleResult("COMPLETED"));

        OrderResult result = orderService.complete(new ConfirmDeliveredCommand(ORDER_ID));

        assertEquals("COMPLETED", result.status());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(integrationEventPublisher).publishOrderCompleted(ORDER_ID);
    }

    @Test
    void rejectByMerchantRequiresOwnerToBeAMerchant() {
        when(merchantQueryPort.findMerchantIdByOwnerId("ghost")).thenReturn(Optional.empty());

        assertThrows(OrderingException.class,
                () -> orderService.rejectByMerchant(new RejectOrderCommand(ORDER_ID, "ghost", "x")));
    }

    @Test
    void rejectByMerchantSucceedsWhenOwnerOwnsTheMerchant() {
        Order order = pendingOrder();
        when(merchantQueryPort.findMerchantIdByOwnerId("owner-1"))
                .thenReturn(Optional.of(MERCHANT_ID));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(mapper.toResult(order)).thenReturn(sampleResult("REJECTED"));

        OrderResult result = orderService.rejectByMerchant(
                new RejectOrderCommand(ORDER_ID, "owner-1", "no stock"));

        assertEquals("REJECTED", result.status());
        verify(integrationEventPublisher).publishOrderCancelled(eq(ORDER_ID),
                eq("MERCHANT_REJECTED"), eq("no stock"),
                eq(OrderingIntegrationEventPublisherPort.CancellationKind.MERCHANT_REJECTED));
    }

    @Test
    void getForRequesterReturnsResultWhenUserOwnsOrder() {
        Order order = pendingOrder();
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(mapper.toResult(order)).thenReturn(sampleResult("PENDING"));

        OrderResult result = orderService.getForRequester(ORDER_ID, USER_ID);

        assertEquals("PENDING", result.status());
    }

    @Test
    void getForRequesterRejectsUnrelatedRequester() {
        Order order = pendingOrder();
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(merchantQueryPort.findMerchantIdByOwnerId("intruder"))
                .thenReturn(Optional.empty());

        assertThrows(OrderingException.class,
                () -> orderService.getForRequester(ORDER_ID, "intruder"));
    }

    @Test
    void listByUserMapsOrdersToResults() {
        Order order = pendingOrder();
        when(orderRepository.findByUser(USER_ID, 20)).thenReturn(List.of(order));
        when(mapper.toResult(order)).thenReturn(sampleResult("PENDING"));

        List<OrderResult> results = orderService.listByUser(USER_ID, 20);

        assertEquals(1, results.size());
        assertEquals("PENDING", results.get(0).status());
    }

    @Test
    void markShippedCommitsReservations() {
        Order order = pendingOrder();
        order.approve("p");
        order.confirmPreparation();
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(mapper.toResult(order)).thenReturn(sampleResult("SHIPPED"));

        OrderResult result = orderService.markShipped(new ConfirmShippedCommand(ORDER_ID, "ship-1"));

        assertEquals("SHIPPED", result.status());
        verify(integrationEventPublisher).publishOrderShipped(ORDER_ID, "ship-1");
    }
}
