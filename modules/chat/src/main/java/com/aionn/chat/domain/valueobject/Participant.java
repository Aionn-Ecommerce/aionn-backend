package com.aionn.chat.domain.valueobject;

import java.time.Instant;
import java.util.Objects;

/**
 * One participant in a conversation. {@code lastReadAt} drives the unread
 * counter â€” every message authored after it is unread for this participant.
 */
public record Participant(
        String userId,
        ParticipantRole role,
        String displayName,
        String avatarUrl,
        Instant joinedAt,
        Instant lastReadAt) {

    public Participant {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(role, "role");
    }

    public Participant withLastReadAt(Instant readAt) {
        return new Participant(userId, role, displayName, avatarUrl, joinedAt, readAt);
    }
}

