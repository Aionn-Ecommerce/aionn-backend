package com.aionn.chat.application.usecase.message;

import com.aionn.chat.application.dto.message.result.MessageResult;
import com.aionn.chat.application.port.in.message.GetMessageQueryPort;
import com.aionn.chat.application.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetMessageUseCase implements GetMessageQueryPort {

    private final MessageService messageService;

    @Override
    public MessageResult execute(String userId, String messageId) {
        return messageService.getForUser(userId, messageId);
    }
}
