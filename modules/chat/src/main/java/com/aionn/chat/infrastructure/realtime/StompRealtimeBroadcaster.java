package com.aionn.chat.infrastructure.realtime;

import com.aionn.chat.application.dto.message.result.MessageResult;
import com.aionn.chat.application.port.out.RealtimeBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Default STOMP broadcaster. Pushes to user destinations:
 * <ul>
 * <li>{@code /user/{userId}/queue/messages} - new messages</li>
 * <li>{@code /user/{userId}/queue/conversation-events} -
 * read/typing/recall</li>
 * </ul>
 *
 * Activated when {@code chat.realtime.provider=stomp} (default).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "chat.realtime", name = "provider", havingValue = "stomp", matchIfMissing = true)
public class StompRealtimeBroadcaster implements RealtimeBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcastMessage(MessageResult message, List<String> recipientIds) {
        for (String recipientId : recipientIds) {
            messagingTemplate.convertAndSendToUser(recipientId, "/queue/messages", message);
        }
    }

    @Override
    public void broadcastConversationRead(String conversationId, String userId, Instant readAt) {
        Map<String, Object> payload = Map.of(
                "type", "CONVERSATION_READ",
                "conversationId", conversationId,
                "userId", userId,
                "readAt", readAt.toString());
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, payload);
    }

    @Override
    public void broadcastTypingChange(String conversationId, String userId, boolean typing) {
        Map<String, Object> payload = Map.of(
                "type", "TYPING",
                "conversationId", conversationId,
                "userId", userId,
                "typing", typing);
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, payload);
    }

    @Override
    public void broadcastMessageRecalled(String conversationId, String messageId) {
        Map<String, Object> payload = Map.of(
                "type", "MESSAGE_RECALLED",
                "conversationId", conversationId,
                "messageId", messageId);
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, payload);
    }
}

