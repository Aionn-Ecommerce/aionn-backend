package com.aionn.notification.domain.valueobject;

/**
 * Drives priority and unsubscribe rules. Security/Transaction notifications
 * cannot be disabled by the user.
 */
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

