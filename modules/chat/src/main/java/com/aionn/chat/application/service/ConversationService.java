package com.aionn.chat.application.service;

import com.aionn.chat.application.dto.conversation.command.ConversationCommands;
import com.aionn.chat.application.dto.conversation.result.ConversationResult;
import com.aionn.chat.application.mapper.ChatResultMapper;
import com.aionn.chat.application.port.out.ConversationPersistencePort;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.chat.application.port.out.MessagePersistencePort;
import com.aionn.chat.application.port.out.RealtimeBroadcaster;
import com.aionn.chat.domain.exception.ChatErrorCode;
import com.aionn.chat.domain.exception.ChatException;
import com.aionn.chat.domain.model.Conversation;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConversationService {

    private final ConversationPersistencePort conversationRepository;
    private final MessagePersistencePort messageRepository;
    private final ChatResultMapper mapper;
    private final EventPublisher eventPublisher;
    private final RealtimeBroadcaster broadcaster;

    public ConversationResult startOrGet(ConversationCommands.StartConversation command) {
        var existing = conversationRepository.findByBuyerAndMerchant(command.buyerId(), command.merchantId());
        if (existing.isPresent()) {
            return toResult(existing.get(), command.startedBy());
        }
        Conversation c = Conversation.start(IdGenerator.ulid(),
                command.buyerId(), command.buyerDisplayName(), command.buyerAvatarUrl(),
                command.merchantId(), command.merchantDisplayName(), command.merchantAvatarUrl(),
                command.startedBy());
        Conversation saved = conversationRepository.save(c);
        eventPublisher.publish(c.pullEvents());
        return toResult(saved, command.startedBy());
    }

    public ConversationResult markRead(ConversationCommands.MarkRead command) {
        Conversation c = required(command.conversationId());
        c.markRead(command.userId());
        Conversation saved = conversationRepository.save(c);
        eventPublisher.publish(c.pullEvents());
        broadcaster.broadcastConversationRead(c.getConversationId(), command.userId(), Instant.now());
        return toResult(saved, command.userId());
    }

    public ConversationResult archive(ConversationCommands.Archive command) {
        Conversation c = required(command.conversationId());
        c.archive(command.userId());
        Conversation saved = conversationRepository.save(c);
        return toResult(saved, command.userId());
    }

    public ConversationResult unarchive(ConversationCommands.Unarchive command) {
        Conversation c = required(command.conversationId());
        c.unarchive(command.userId());
        Conversation saved = conversationRepository.save(c);
        return toResult(saved, command.userId());
    }

    public ConversationResult joinSupport(ConversationCommands.JoinSupport command) {
        Conversation c = required(command.conversationId());
        c.joinSupport(command.supportUserId(), command.displayName(), command.avatarUrl());
        Conversation saved = conversationRepository.save(c);
        return toResult(saved, command.supportUserId());
    }

    @Transactional(readOnly = true)
    public ConversationResult getForUser(String userId, String conversationId) {
        Conversation c = required(conversationId);
        c.requireParticipant(userId);
        return toResult(c, userId);
    }

    @Transactional(readOnly = true)
    public List<ConversationResult> listForUser(String userId, boolean includeArchived, int limit) {
        return conversationRepository.findByUser(userId, includeArchived, limit).stream()
                .map(c -> toResult(c, userId))
                .toList();
    }

    private Conversation required(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CONVERSATION_NOT_FOUND));
    }

    ConversationResult toResult(Conversation c, String forUserId) {
        Instant lastRead = c.participantLastReadMap().get(forUserId);
        Instant since = lastRead == null ? c.getCreatedAt() : lastRead;
        long unread = messageRepository.countUnread(c.getConversationId(), forUserId, since);
        return mapper.toResult(c, unread);
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Long> getUnreadCounts(String userId) {
        List<Conversation> activeConversations = conversationRepository.findByUser(userId, false, 100);
        java.util.Map<String, Long> counts = new java.util.HashMap<>();
        for (Conversation c : activeConversations) {
            Instant lastRead = c.participantLastReadMap().get(userId);
            Instant since = lastRead == null ? c.getCreatedAt() : lastRead;
            long unread = messageRepository.countUnread(c.getConversationId(), userId, since);
            counts.put(c.getConversationId(), unread);
        }
        return counts;
    }
}

