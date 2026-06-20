package com.aionn.chat.application.usecase.message;

import com.aionn.chat.application.dto.message.command.MessageCommands;
import com.aionn.chat.application.dto.message.result.MessageResult;
import com.aionn.chat.application.port.in.message.MarkDeliveredInputPort;
import com.aionn.chat.application.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarkDeliveredUseCase implements MarkDeliveredInputPort {

    private final MessageService messageService;

    @Override
    public MessageResult execute(MessageCommands.DeliverMessage command) {
        return messageService.markDelivered(command);
    }
}
