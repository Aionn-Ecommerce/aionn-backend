package com.aionn.chat.application.service;

import com.aionn.chat.application.dto.autoreply.command.AutoReplyCommands;
import com.aionn.chat.application.dto.autoreply.result.AutoReplyResult;
import com.aionn.chat.application.mapper.ChatResultMapper;
import com.aionn.chat.application.port.out.MerchantAutoReplyPersistencePort;
import com.aionn.chat.domain.exception.ChatErrorCode;
import com.aionn.chat.domain.exception.ChatException;
import com.aionn.chat.domain.model.MerchantAutoReply;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.sharedkernel.integration.port.catalog.MerchantQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AutoReplyService {

    private final MerchantAutoReplyPersistencePort repository;
    private final ChatResultMapper mapper;
    private final EventPublisher eventPublisher;
    private final MerchantQueryPort merchantQueryPort;

    public AutoReplyResult update(AutoReplyCommands.UpdateAutoReply command) {
        ensureCallerOwnsMerchant(command.ownerId(), command.merchantId());
        MerchantAutoReply autoReply = repository.findByMerchantId(command.merchantId())
                .orElseGet(() -> MerchantAutoReply.create(command.merchantId()));
        autoReply.update(command.enabled(), command.greeting(), command.awayMessage(),
                command.workingHourStart(), command.workingHourEnd(), command.workingDays());
        MerchantAutoReply saved = repository.save(autoReply);
        eventPublisher.publish(autoReply.pullEvents());
        return mapper.toResult(saved);
    }

    @Transactional(readOnly = true)
    public AutoReplyResult get(String ownerId, String merchantId) {
        ensureCallerOwnsMerchant(ownerId, merchantId);
        MerchantAutoReply autoReply = repository.findByMerchantId(merchantId)
                .orElseGet(() -> MerchantAutoReply.create(merchantId));
        return mapper.toResult(autoReply);
    }

    private void ensureCallerOwnsMerchant(String ownerId, String merchantId) {
        String resolved = merchantQueryPort.findMerchantIdByOwnerId(ownerId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.AUTO_REPLY_FORBIDDEN,
                        "Caller is not registered as a merchant owner"));
        if (!resolved.equals(merchantId)) {
            throw new ChatException(ChatErrorCode.AUTO_REPLY_FORBIDDEN,
                    "Caller does not own the requested merchant");
        }
    }
}
