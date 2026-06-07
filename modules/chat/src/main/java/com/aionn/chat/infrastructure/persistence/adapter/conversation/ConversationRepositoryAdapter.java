package com.aionn.chat.infrastructure.persistence.adapter.conversation;

import com.aionn.chat.application.port.out.ConversationRepository;
import com.aionn.chat.domain.model.Conversation;
import com.aionn.chat.infrastructure.persistence.entity.ConversationEntity;
import com.aionn.chat.infrastructure.persistence.mapper.ConversationDomainMapper;
import com.aionn.chat.infrastructure.persistence.repository.ConversationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ConversationRepositoryAdapter implements ConversationRepository {

    private final ConversationJpaRepository jpa;
    private final ConversationDomainMapper mapper;

    @Override
    public Conversation save(Conversation conversation) {
        ConversationEntity existing = jpa.findById(conversation.getConversationId()).orElse(null);
        return mapper.toDomain(jpa.save(mapper.toEntity(conversation, existing)));
    }

    @Override
    public Optional<Conversation> findById(String conversationId) {
        return jpa.findById(conversationId).map(mapper::toDomain);
    }

    @Override
    public Optional<Conversation> findByBuyerAndMerchant(String buyerId, String merchantId) {
        return jpa.findByBuyerIdAndMerchantId(buyerId, merchantId).map(mapper::toDomain);
    }

    @Override
    public List<Conversation> findByUser(String userId, boolean includeArchived, int limit) {
        return jpa.findByUser(userId, includeArchived, PageRequest.of(0, Math.max(1, limit))).stream()
                .map(mapper::toDomain)
                .toList();
    }
}

