package com.aionn.ordering.infrastructure.gateway;

import com.aionn.ordering.application.port.out.PaymentGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Stub for the future Payment integration. Fails closed. */
@Component
@ConditionalOnProperty(prefix = "ordering.payment", name = "provider", havingValue = "remote")
public class RemotePaymentGateway implements PaymentGateway {

    @Override
    public PaymentAuthorization authorize(String orderId, String userId, String paymentMethodId,
            BigDecimal amount, String currency) {
        throw new UnsupportedOperationException("Remote PaymentGateway is not implemented yet");
    }

    @Override
    public void refund(String paymentId, BigDecimal amount, String currency, String reason) {
        throw new UnsupportedOperationException("Remote PaymentGateway is not implemented yet");
    }
}

