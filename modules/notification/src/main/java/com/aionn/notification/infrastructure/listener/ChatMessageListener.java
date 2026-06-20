package com.aionn.notification.infrastructure.listener;

import com.aionn.notification.application.dto.notification.command.NotificationCommands;
import com.aionn.notification.application.port.out.observability.NotificationMetricsPort;
import com.aionn.notification.application.service.NotificationDispatchService;
import com.aionn.notification.domain.valueobject.NotificationCategory;
import com.aionn.notification.domain.valueobject.NotificationChannel;
import com.aionn.sharedkernel.integration.event.chat.MessageSentIntegrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageListener {

    private final NotificationDispatchService dispatchService;
    private final NotificationMetricsPort metrics;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMessageSent(MessageSentIntegrationEvent event) {
        if (event.recipientId() == null || event.recipientId().isBlank()) {
            log.debug("Skipping push notification - no recipient in event {}", event.eventId());
            return;
        }

        try {
            Map<String, String> context = Map.of(
                    "conversationId", event.conversationId(),
                    "messageId", event.messageId(),
                    "senderDisplayName",
                    event.senderDisplayName() != null ? event.senderDisplayName() : event.senderId(),
                    "preview", event.messagePreview() != null ? event.messagePreview() : "");

            dispatchService.sendDirectByEvent(new NotificationCommands.SendDirectByEvent(
                    event.recipientId(),
                    "CHAT_MESSAGE_RECEIVED", // Match template event_type in database
                    NotificationCategory.SYSTEM,
                    NotificationChannel.PUSH,
                    null, // recipient - will be resolved from userId
                    null, // locale - will use default
                    null, // campaignId
                    context));

            metrics.deliveryOutcome("push", "success");
            log.debug("Push notification sent for message {} to recipient {}",
                    event.messageId(), event.recipientId());
        } catch (Exception ex) {
            log.warn("Failed to send push notification for message {}: {}",
                    event.messageId(), ex.getMessage());
            metrics.deliveryOutcome("push", "failed");
        }
    }
}
