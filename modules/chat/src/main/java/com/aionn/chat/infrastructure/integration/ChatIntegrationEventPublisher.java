package com.aionn.chat.infrastructure.integration;

import com.aionn.chat.application.port.out.integration.ChatIntegrationEventPublisherPort;
import com.aionn.sharedkernel.integration.event.chat.MessageSentIntegrationEvent;
import com.aionn.sharedkernel.integration.publisher.IntegrationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ChatIntegrationEventPublisher implements ChatIntegrationEventPublisherPort {

    private final IntegrationEventPublisher integrationEventPublisher;

    @Override
    public void publishMessageSent(String conversationId, String messageId, String senderId,
            String recipientId, String senderDisplayName, String messagePreview) {
        integrationEventPublisher.publish(new MessageSentIntegrationEvent(
                null, conversationId, messageId, senderId, recipientId,
                senderDisplayName, messagePreview, Instant.now()));
    }
}
