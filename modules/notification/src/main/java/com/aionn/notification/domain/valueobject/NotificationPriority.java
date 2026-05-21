package com.aionn.notification.domain.valueobject;

/**
 * UC8.2 - priority dictates retry policy + queue ordering. Derived from
 * category + caller hint.
 */
public enum NotificationPriority {
    /** Security alerts, fraud, login from new device. */
    CRITICAL,
    /** Order placed, payment processed, shipping update. */
    HIGH,
    /** Receipts, password change confirmations. */
    NORMAL,
    /** Marketing campaigns, recommendations. */
    LOW;

    public static NotificationPriority forCategory(NotificationCategory category) {
        return switch (category) {
            case SECURITY -> CRITICAL;
            case TRANSACTION, SHIPPING -> HIGH;
            case SYSTEM -> NORMAL;
            case PROMOTION -> LOW;
        };
    }
}

