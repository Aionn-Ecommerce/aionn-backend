package com.aionn.chat.domain.valueobject;

/**
 * Per-message lifecycle.
 *
 * <pre>
 *   SENT       - persisted and broadcast over WebSocket
 *   DELIVERED  - acknowledged by the recipient's client
 *   READ       - recipient opened the conversation
 *   RECALLED   - sender recalled within the time window
 * </pre>
 */
public enum MessageStatus {
    SENT,
    DELIVERED,
    READ,
    RECALLED;

    public boolean canTransitionTo(MessageStatus next) {
        return switch (this) {
            case SENT -> next == DELIVERED || next == READ || next == RECALLED;
            case DELIVERED -> next == READ || next == RECALLED;
            case READ -> next == RECALLED;
            case RECALLED -> false;
        };
    }
}

