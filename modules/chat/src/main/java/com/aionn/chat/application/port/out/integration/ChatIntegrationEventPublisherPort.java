package com.aionn.chat.application.port.out.integration;

public interface ChatIntegrationEventPublisherPort {

    /**
     * Fire one {@code MessageSentIntegrationEvent} per offline recipient so
     * notification can deliver an email / SMS / push fallback. Callers should
     * skip recipients who are currently online (delivered via STOMP already).
     */
    void publishMessageSent(
            String conversationId,
            String messageId,
            String senderId,
            String recipientId,
            String senderDisplayName,
            String messagePreview);
}
