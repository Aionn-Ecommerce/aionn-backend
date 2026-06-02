package com.aionn.sharedkernel.integration.event.chat;

import com.aionn.sharedkernel.integration.event.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when a chat message has been sent.
 * 
 * <p>
 * Consumers may use this event to:
 * </p>
 * <ul>
 * <li>Send push notifications to offline recipients</li>
 * <li>Update conversation analytics</li>
 * <li>Trigger auto-reply if configured</li>
 * </ul>
 */
public record MessageSentIntegrationEvent(
        String eventId,
        String conversationId,
        String messageId,
        String senderId,
        String recipientId,
        String senderDisplayName,
        String messagePreview,
        Instant occurredAt) implements IntegrationEvent {

    public MessageSentIntegrationEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
    }
}
