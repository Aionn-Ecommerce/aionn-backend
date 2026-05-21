package com.aionn.payment.domain.valueobject;

/**
 * Lifecycle of a stored payment method.
 *
 * <pre>
 *   LINKED â†’ VERIFIED â†’ REMOVED
 *   LINKED â†’ REMOVED
 * </pre>
 */
public enum PaymentMethodStatus {
    LINKED,
    VERIFIED,
    REMOVED;

    public boolean canTransitionTo(PaymentMethodStatus next) {
        return switch (this) {
            case LINKED -> next == VERIFIED || next == REMOVED;
            case VERIFIED -> next == REMOVED;
            case REMOVED -> false;
        };
    }
}

