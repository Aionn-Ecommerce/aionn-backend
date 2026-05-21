package com.aionn.payment.infrastructure.provider;

import com.aionn.payment.application.port.out.PaymentProviderClient;
import com.aionn.payment.domain.exception.PaymentErrorCode;
import com.aionn.payment.domain.exception.PaymentException;
import com.aionn.payment.domain.valueobject.PaymentGatewayKind;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Stripe adapter. Activated only when
 * {@code payment.provider.stripe.enabled=true}.
 * Wires through Stripe's official SDK - the actual API call is left as a
 * stub (throws UnsupportedOperationException) until the team supplies real
 * API keys; the bean is still registered so the router can route to it.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "payment.provider.stripe", name = "enabled", havingValue = "true")
public class StripePaymentProviderClient implements PaymentProviderClient {

    @Override
    public PaymentGatewayKind kind() {
        return PaymentGatewayKind.STRIPE;
    }

    @Override
    public Authorization authorize(AuthorizationRequest request) {
        // TODO: wire com.stripe.model.PaymentIntent.create(...)
        throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR,
                "Stripe authorize is not implemented yet");
    }

    @Override
    public Refund refund(RefundRequest request) {
        throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR,
                "Stripe refund is not implemented yet");
    }

    @Override
    public String generateInvoice(String paymentId, String orderId, BigDecimal amount, String currency) {
        throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR,
                "Stripe invoice generation is not implemented yet");
    }

    @Override
    public WebhookEvent verifyAndParse(String rawBody, String signatureHeader) {
        throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR,
                "Stripe webhook verification is not implemented yet");
    }
}

