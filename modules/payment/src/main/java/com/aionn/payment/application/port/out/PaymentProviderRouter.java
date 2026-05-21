package com.aionn.payment.application.port.out;

import com.aionn.payment.domain.valueobject.PaymentGatewayKind;

/**
 * Picks the right {@link PaymentProviderClient} based on the requested
 * {@link PaymentGatewayKind}. Implementations typically aggregate every bean
 * found in the application context.
 */
public interface PaymentProviderRouter {
    PaymentProviderClient route(PaymentGatewayKind kind);
}

