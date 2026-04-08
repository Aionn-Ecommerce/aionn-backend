package com.ecommerce.identity.domain.valueobject;

public enum KycStatus {
    DRAFT,
    SUBMITTED,
    IN_REVIEW,
    APPROVED,
    REJECTED;

    public boolean canTransitionTo(KycStatus newStatus) {
        return switch (this) {
            case DRAFT -> newStatus == SUBMITTED;
            case SUBMITTED -> newStatus == IN_REVIEW || newStatus == REJECTED;
            case IN_REVIEW -> newStatus == APPROVED || newStatus == REJECTED;
            case APPROVED, REJECTED -> false;
        };
    }

    public boolean canBeCancelled() {
        return this == DRAFT || this == SUBMITTED;
    }
}
