package com.aionn.ordering.application.port.out;

import java.math.BigDecimal;

/**
 * Outbound port to Payment bounded context. Built as 2-impl: assume-success
 * for dev/test and a remote stub for the future.
 */
public interface PaymentGateway {

    PaymentAuthorization authorize(String orderId, String userId, String paymentMethodId,
            BigDecimal amount, String currency);

    void refund(String paymentId, BigDecimal amount, String currency, String reason);

    record PaymentAuthorization(String paymentId, boolean approved, String declineReason) {
    }
}

