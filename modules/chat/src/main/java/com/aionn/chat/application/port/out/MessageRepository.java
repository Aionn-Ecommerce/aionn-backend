package com.aionn.chat.application.port.out;

import com.aionn.chat.domain.model.Message;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MessageRepository {

    Message save(Message message);

    Optional<Message> findById(String messageId);

    /** Reverse-chronological pagination ("load older"). */
    List<Message> findByConversationBefore(String conversationId, Instant before, int limit);

    /** Newest first. */
    List<Message> findByConversationLatest(String conversationId, int limit);

    /**
     * Count messages from anyone *except* {@code userId} after
     * {@code afterInstant}.
     */
    long countUnread(String conversationId, String userId, Instant afterInstant);
}

