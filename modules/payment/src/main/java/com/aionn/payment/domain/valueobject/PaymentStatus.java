package com.aionn.payment.domain.valueobject;

/**
 * Payment lifecycle.
 *
 * <pre>
 *   INITIATED â†’ PROCESSING â†’ PAID â†’ REFUNDED (partial or full)
 *   INITIATED â†’ FAILED
 *   PROCESSING â†’ FAILED
 * </pre>
 */
public enum PaymentStatus {
    INITIATED,
    PROCESSING,
    PAID,
    FAILED,
    REFUNDED;

    public boolean canTransitionTo(PaymentStatus next) {
        return switch (this) {
            case INITIATED -> next == PROCESSING || next == PAID || next == FAILED;
            case PROCESSING -> next == PAID || next == FAILED;
            case PAID -> next == REFUNDED;
            case REFUNDED, FAILED -> false;
        };
    }

    public boolean isTerminal() {
        return this == FAILED || this == REFUNDED;
    }
}

