package com.aionn.notification.domain.valueobject;

/**
 * Notification lifecycle.
 *
 * <pre>
 *   PENDING â†’ SENT (delivery attempt succeeded)
 *           â†’ FAILED (max retries exhausted)
 *   SENT     â†’ READ (user opened it)
 *   any non-DELETED â†’ DELETED (user soft-removes from inbox)
 * </pre>
 */
public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED,
    READ,
    DELETED;

    public boolean canTransitionTo(NotificationStatus next) {
        return switch (this) {
            case PENDING -> next == SENT || next == FAILED || next == DELETED;
            case SENT -> next == READ || next == DELETED;
            case READ -> next == DELETED;
            case FAILED -> next == DELETED;
            case DELETED -> false;
        };
    }
}

