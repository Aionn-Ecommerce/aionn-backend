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
 * VNPay adapter for domestic payments. Activated when
 * {@code payment.provider.vnpay.enabled=true}; same stub-style as the Stripe
 * adapter â€” return URL + secure-hash flow are left for the team to fill in
 * once the merchant ID + secret key are available.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "payment.provider.vnpay", name = "enabled", havingValue = "true")
public class VnpayPaymentProviderClient implements PaymentProviderClient {

    @Override
    public PaymentGatewayKind kind() {
        return PaymentGatewayKind.VNPAY;
    }

    @Override
    public Authorization authorize(AuthorizationRequest request) {
        // VNPay is async: returns a redirect URL. Real impl builds the URL
        // signed with HMAC-SHA512 and returns captured=false + authUrl.
        throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR,
                "VNPay authorize is not implemented yet");
    }

    @Override
    public Refund refund(RefundRequest request) {
        throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR,
                "VNPay refund is not implemented yet");
    }

    @Override
    public String generateInvoice(String paymentId, String orderId, BigDecimal amount, String currency) {
        throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR,
                "VNPay invoice is not implemented yet");
    }

    @Override
    public WebhookEvent verifyAndParse(String rawBody, String signatureHeader) {
        throw new PaymentException(PaymentErrorCode.PAYMENT_GATEWAY_ERROR,
                "VNPay webhook is not implemented yet");
    }
}

