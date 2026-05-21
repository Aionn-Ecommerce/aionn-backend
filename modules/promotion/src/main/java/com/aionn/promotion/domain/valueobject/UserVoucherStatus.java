package com.aionn.promotion.domain.valueobject;

/**
 * Per-user voucher claim lifecycle.
 *
 * <pre>
 *   CLAIMED â†’ RESERVED â†’ APPLIED   (order paid)
 *   CLAIMED â†’ RESERVED â†’ RELEASED  (order cancelled / expired)
 *   CLAIMED â†’ EXPIRED              (validUntil passed without use)
 * </pre>
 */
public enum UserVoucherStatus {
    CLAIMED,
    RESERVED,
    APPLIED,
    RELEASED,
    EXPIRED;

    public boolean canTransitionTo(UserVoucherStatus next) {
        return switch (this) {
            case CLAIMED -> next == RESERVED || next == EXPIRED || next == RELEASED;
            case RESERVED -> next == APPLIED || next == RELEASED || next == EXPIRED;
            case RELEASED -> next == RESERVED || next == EXPIRED; // can re-reserve after release if still valid
            case APPLIED, EXPIRED -> false;
        };
    }
}

