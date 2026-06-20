package com.aionn.chat.application.usecase.conversation;

import com.aionn.chat.application.dto.conversation.command.ConversationCommands;
import com.aionn.chat.application.dto.conversation.result.ConversationResult;
import com.aionn.chat.application.port.in.conversation.MarkConversationReadInputPort;
import com.aionn.chat.application.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarkConversationReadUseCase implements MarkConversationReadInputPort {

    private final ConversationService conversationService;

    @Override
    public ConversationResult execute(ConversationCommands.MarkRead command) {
        return conversationService.markRead(command);
    }
}
