package com.aionn.chat.application.usecase.autoreply;

import com.aionn.chat.application.dto.autoreply.command.AutoReplyCommands;
import com.aionn.chat.application.dto.autoreply.result.AutoReplyResult;
import com.aionn.chat.application.port.in.autoreply.UpdateAutoReplyInputPort;
import com.aionn.chat.application.service.AutoReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateAutoReplyUseCase implements UpdateAutoReplyInputPort {

    private final AutoReplyService autoReplyService;

    @Override
    public AutoReplyResult execute(AutoReplyCommands.UpdateAutoReply command) {
        return autoReplyService.update(command);
    }
}
