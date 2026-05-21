package com.aionn.chat.adapter.websocket;

import com.aionn.chat.application.dto.message.command.MessageCommands;
import com.aionn.chat.application.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * STOMP destinations for low-latency client signals (typing, delivery
 * receipts, read receipts). Everything that mutates state still goes through
 * REST so we keep one consistent transactional path; STOMP is the side
 * channel.
 *
 * <p>
 * Destinations:
 * <ul>
 * <li>{@code /app/chat/conversations/{conversationId}/typing}</li>
 * <li>{@code /app/chat/messages/{messageId}/delivered}</li>
 * <li>{@code /app/chat/messages/{messageId}/read}</li>
 * </ul>
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final MessageService messageService;

    @MessageMapping("/chat/conversations/{conversationId}/typing")
    public void typing(@DestinationVariable String conversationId,
            @Payload TypingPayload payload, Principal principal) {
        if (principal == null)
            return;
        messageService.setTyping(new MessageCommands.SetTyping(
                principal.getName(), conversationId, payload.typing()));
    }

    @MessageMapping("/chat/messages/{messageId}/delivered")
    public void delivered(@DestinationVariable String messageId, Principal principal) {
        if (principal == null)
            return;
        messageService.markDelivered(new MessageCommands.DeliverMessage(principal.getName(), messageId));
    }

    @MessageMapping("/chat/messages/{messageId}/read")
    public void read(@DestinationVariable String messageId, Principal principal) {
        if (principal == null)
            return;
        messageService.markRead(new MessageCommands.ReadMessage(principal.getName(), messageId));
    }

    public record TypingPayload(boolean typing) {
    }
}

