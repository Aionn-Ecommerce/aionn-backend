package com.aionn.chat.application.usecase.message;

import com.aionn.chat.application.dto.message.command.MessageCommands;
import com.aionn.chat.application.port.in.message.SetTypingInputPort;
import com.aionn.chat.application.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SetTypingUseCase implements SetTypingInputPort {

    private final MessageService messageService;

    @Override
    public void execute(MessageCommands.SetTyping command) {
        messageService.setTyping(command);
    }
}
