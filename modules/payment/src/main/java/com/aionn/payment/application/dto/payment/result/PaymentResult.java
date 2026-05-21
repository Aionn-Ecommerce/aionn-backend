package com.aionn.payment.application.dto.payment.result;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResult(
        String paymentId,
        String orderId,
        String userId,
        String paymentMethodId,
        BigDecimal amount,
        BigDecimal refundedAmount,
        String currency,
        String gateway,
        String status,
        String transactionNo,
        String invoiceUrl,
        String errorCode,
        String errorReason,
        Instant createdAt,
        Instant updatedAt,
        Instant paidAt,
        Instant failedAt) {
}

