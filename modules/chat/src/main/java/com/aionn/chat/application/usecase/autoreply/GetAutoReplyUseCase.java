package com.aionn.chat.application.usecase.autoreply;

import com.aionn.chat.application.dto.autoreply.result.AutoReplyResult;
import com.aionn.chat.application.port.in.autoreply.GetAutoReplyQueryPort;
import com.aionn.chat.application.service.AutoReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAutoReplyUseCase implements GetAutoReplyQueryPort {

    private final AutoReplyService autoReplyService;

    @Override
    public AutoReplyResult execute(String ownerId, String merchantId) {
        return autoReplyService.get(ownerId, merchantId);
    }
}
