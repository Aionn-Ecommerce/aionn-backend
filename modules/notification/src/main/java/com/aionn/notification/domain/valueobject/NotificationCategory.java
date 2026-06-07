package com.aionn.notification.domain.valueobject;

public enum NotificationCategory {
    SECURITY,
    TRANSACTION,
    SHIPPING,
    PROMOTION,
    SYSTEM;

    public boolean isMandatory() {
        return this == SECURITY || this == TRANSACTION;
    }
}

