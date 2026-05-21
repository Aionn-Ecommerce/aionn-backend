package com.aionn.payment.application.port.out;

import com.aionn.payment.domain.valueobject.PaymentGatewayKind;

import java.math.BigDecimal;

/**
 * Provider-side client. Each provider (Stripe, VNPay, Mock) gets its own
 * implementation chosen by the {@link PaymentProviderRouter} based on
 * {@link PaymentGatewayKind}.
 */
public interface PaymentProviderClient {

    PaymentGatewayKind kind();

    /**
     * Authorize + charge synchronously. Some gateways reply async (VNPay
     * returns a redirect URL); for those the implementation returns
     * {@code authUrl} populated and {@code captured=false}.
     */
    Authorization authorize(AuthorizationRequest request);

    Refund refund(RefundRequest request);

    /**
     * Generate (or fetch) the invoice for a successful payment. Returns the
     * downloadable URL.
     */
    String generateInvoice(String paymentId, String orderId, BigDecimal amount, String currency);

    /** Verify that a webhook payload is genuine and parse it. */
    WebhookEvent verifyAndParse(String rawBody, String signatureHeader);

    record AuthorizationRequest(
            String paymentId,
            String orderId,
            String userId,
            String paymentMethodToken,
            BigDecimal amount,
            String currency,
            String idempotencyKey,
            String returnUrl) {
    }

    record Authorization(
            boolean captured,
            String transactionNo,
            String authUrl,
            String declineCode,
            String declineReason) {
    }

    record RefundRequest(
            String paymentId,
            String transactionNo,
            BigDecimal amount,
            String currency,
            String reason) {
    }

    record Refund(boolean accepted, String refundTransactionNo, String declineReason) {
    }

    record WebhookEvent(
            String type,
            String paymentId,
            String transactionNo,
            BigDecimal amount,
            String currency,
            boolean success,
            String errorCode,
            String errorReason) {
    }
}

