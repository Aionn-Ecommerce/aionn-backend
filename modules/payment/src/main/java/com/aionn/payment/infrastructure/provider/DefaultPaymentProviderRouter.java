package com.aionn.payment.infrastructure.provider;

import com.aionn.payment.application.port.out.PaymentProviderClient;
import com.aionn.payment.application.port.out.PaymentProviderRouter;
import com.aionn.payment.domain.exception.PaymentErrorCode;
import com.aionn.payment.domain.exception.PaymentException;
import com.aionn.payment.domain.valueobject.PaymentGatewayKind;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates every {@link PaymentProviderClient} bean in the context and
 * indexes them by their {@link PaymentGatewayKind}. If the requested kind is
 * not wired (e.g. {@code STRIPE} when stripe.enabled=false) the router throws
 * a domain exception so the caller can surface a clean error.
 */
@Component
@RequiredArgsConstructor
public class DefaultPaymentProviderRouter implements PaymentProviderRouter {

    private final List<PaymentProviderClient> clients;

    private Map<PaymentGatewayKind, PaymentProviderClient> index;

    @Override
    public PaymentProviderClient route(PaymentGatewayKind kind) {
        if (index == null) {
            Map<PaymentGatewayKind, PaymentProviderClient> map = new EnumMap<>(PaymentGatewayKind.class);
            for (PaymentProviderClient c : clients) {
                map.put(c.kind(), c);
            }
            index = map;
        }
        PaymentProviderClient client = index.get(kind);
        if (client == null) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR,
                    "No provider client wired for " + kind);
        }
        return client;
    }
}

