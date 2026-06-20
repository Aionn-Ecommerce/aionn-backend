package com.aionn.chat.application.usecase.message;

import com.aionn.chat.application.dto.message.command.MessageCommands;
import com.aionn.chat.application.dto.message.result.MessageResult;
import com.aionn.chat.application.port.in.message.RecallMessageInputPort;
import com.aionn.chat.application.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecallMessageUseCase implements RecallMessageInputPort {

    private final MessageService messageService;

    @Override
    public MessageResult execute(MessageCommands.RecallMessage command) {
        return messageService.recall(command);
    }
}
