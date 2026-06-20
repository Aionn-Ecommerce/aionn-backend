package com.aionn.chat.application.usecase.message;

import com.aionn.chat.application.dto.message.result.MessageResult;
import com.aionn.chat.application.policy.ChatMessagePolicy;
import com.aionn.chat.application.port.in.message.ListMessagesQueryPort;
import com.aionn.chat.application.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListMessagesUseCase implements ListMessagesQueryPort {

    private final MessageService messageService;
    private final ChatMessagePolicy messagePolicy;

    @Override
    public List<MessageResult> execute(String userId, String conversationId, Instant before, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), messagePolicy.getListMaxLimit());
        if (before == null) {
            return messageService.listLatest(userId, conversationId, safeLimit);
        }
        return messageService.listBefore(userId, conversationId, before, safeLimit);
    }
}
