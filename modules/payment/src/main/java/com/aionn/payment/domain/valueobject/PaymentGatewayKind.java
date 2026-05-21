package com.aionn.payment.domain.valueobject;

/**
 * Logical gateway. The actual API client is wired in infrastructure based on
 * the configured property; this enum is just for routing / reporting.
 */
public enum PaymentGatewayKind {
    STRIPE,
    VNPAY,
    MOCK
}

