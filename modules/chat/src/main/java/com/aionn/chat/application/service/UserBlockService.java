package com.aionn.chat.application.service;

import com.aionn.chat.application.dto.block.command.BlockCommands;
import com.aionn.chat.application.dto.block.result.BlockResult;
import com.aionn.chat.application.mapper.ChatResultMapper;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.chat.application.port.out.UserBlockPersistencePort;
import com.aionn.chat.domain.exception.ChatErrorCode;
import com.aionn.chat.domain.exception.ChatException;
import com.aionn.chat.domain.model.UserBlock;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserBlockService {

    private final UserBlockPersistencePort repository;
    private final ChatResultMapper mapper;
    private final EventPublisher eventPublisher;

    public BlockResult block(BlockCommands.BlockUser command) {
        var existing = repository.findActive(command.blockerId(), command.blockedId());
        if (existing.isPresent()) {
            return mapper.toResult(existing.get());
        }
        UserBlock block = UserBlock.block(IdGenerator.ulid(),
                command.blockerId(), command.blockedId(), command.reason());
        UserBlock saved = repository.save(block);
        eventPublisher.publish(block.pullEvents());
        return mapper.toResult(saved);
    }

    public BlockResult unblock(BlockCommands.UnblockUser command) {
        UserBlock block = repository.findActive(command.blockerId(), command.blockedId())
                .orElseThrow(() -> new ChatException(ChatErrorCode.BLOCK_NOT_FOUND));
        block.unblock();
        UserBlock saved = repository.save(block);
        eventPublisher.publish(block.pullEvents());
        return mapper.toResult(saved);
    }

    @Transactional(readOnly = true)
    public List<BlockResult> listMyBlocks(String userId) {
        return repository.findByBlocker(userId).stream().map(mapper::toResult).toList();
    }
}

