package com.aionn.ordering.infrastructure.gateway;

import com.aionn.ordering.application.port.out.PaymentGateway;
import com.aionn.sharedkernel.integration.port.payment.PaymentInitiatePort;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PaymentInitiatePortGateway implements PaymentGateway {

    private final PaymentInitiatePort paymentInitiatePort;

    @Value("${ordering.payment.gateway-kind:STRIPE}")
    private String defaultGatewayKind;

    @Override
    public PaymentAuthorization authorize(String orderId, String userId, String paymentMethodId,
            BigDecimal amount, String currency) {
        PaymentInitiatePort.InitResult result = paymentInitiatePort.initPayment(
                orderId, userId, paymentMethodId, amount, currency,
                defaultGatewayKind, IdGenerator.ulid());
        if (result.captured()) {
            return new PaymentAuthorization(result.paymentId(), true, null);
        }
        return new PaymentAuthorization(result.paymentId(), false,
                result.redirectUrl() != null ? "async-redirect-required" : "not-captured");
    }

    @Override
    public void refund(String paymentId, BigDecimal amount, String currency, String reason) {
        paymentInitiatePort.refund(paymentId, amount, currency, reason);
    }
}
