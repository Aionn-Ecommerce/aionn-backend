package com.aionn.chat.application.service;

import com.aionn.chat.application.dto.autoreply.command.AutoReplyCommands;
import com.aionn.chat.application.dto.autoreply.result.AutoReplyResult;
import com.aionn.chat.application.mapper.ChatResultMapper;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.chat.application.port.out.MerchantAutoReplyRepository;
import com.aionn.chat.domain.model.MerchantAutoReply;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AutoReplyService {

    private final MerchantAutoReplyRepository repository;
    private final ChatResultMapper mapper;
    private final EventPublisher eventPublisher;

    public AutoReplyResult update(AutoReplyCommands.UpdateAutoReply command) {
        MerchantAutoReply autoReply = repository.findByMerchantId(command.merchantId())
                .orElseGet(() -> MerchantAutoReply.create(command.merchantId()));
        autoReply.update(command.enabled(), command.greeting(), command.awayMessage(),
                command.workingHourStart(), command.workingHourEnd(), command.workingDays());
        MerchantAutoReply saved = repository.save(autoReply);
        eventPublisher.publish(autoReply.pullEvents());
        return mapper.toResult(saved);
    }

    @Transactional(readOnly = true)
    public AutoReplyResult get(String merchantId) {
        MerchantAutoReply autoReply = repository.findByMerchantId(merchantId)
                .orElseGet(() -> MerchantAutoReply.create(merchantId));
        return mapper.toResult(autoReply);
    }
}

