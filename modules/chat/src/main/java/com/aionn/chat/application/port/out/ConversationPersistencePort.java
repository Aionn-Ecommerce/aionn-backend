package com.aionn.chat.application.port.out;

import com.aionn.chat.domain.model.Conversation;

import java.util.List;
import java.util.Optional;

public interface ConversationPersistencePort {

    Conversation save(Conversation conversation);

    Optional<Conversation> findById(String conversationId);

    Optional<Conversation> findByBuyerAndMerchant(String buyerId, String merchantId);

    List<Conversation> findByUser(String userId, boolean includeArchived, int limit);
}

