package com.aionn.ucp.domain.model;

public enum CheckoutSessionStatus {

    INCOMPLETE,
    READY_FOR_COMPLETE,
    REQUIRES_ESCALATION,
    COMPLETE_IN_PROGRESS,
    COMPLETED,
    CANCELED;

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELED;
    }

    public String toWireFormat() {
        return name().toLowerCase();
    }
}
