package com.aionn.chat.domain.model;

import com.aionn.sharedkernel.domain.Guard;
import com.aionn.sharedkernel.domain.model.AggregateRoot;
import com.aionn.chat.domain.event.ChatEvents;
import com.aionn.chat.domain.exception.ChatErrorCode;
import com.aionn.chat.domain.exception.ChatException;
import com.aionn.chat.domain.valueobject.MessageType;
import com.aionn.chat.domain.valueobject.Participant;
import com.aionn.chat.domain.valueobject.ParticipantRole;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
public class Conversation extends AggregateRoot {

    private final String conversationId;
    private final String buyerId;
    private final String merchantId;
    private final List<Participant> participants;
    private String lastMessageId;
    private String lastMessagePreview;
    private MessageType lastMessageType;
    private String lastMessageSenderId;
    private Instant lastMessageAt;
    private boolean archived;
    private final Instant createdAt;
    private Instant updatedAt;

    public Conversation(
            String conversationId,
            String buyerId,
            String merchantId,
            List<Participant> participants,
            String lastMessageId,
            String lastMessagePreview,
            MessageType lastMessageType,
            String lastMessageSenderId,
            Instant lastMessageAt,
            boolean archived,
            Instant createdAt,
            Instant updatedAt) {
        this.conversationId = conversationId;
        this.buyerId = buyerId;
        this.merchantId = merchantId;
        this.participants = new ArrayList<>(participants == null ? List.of() : participants);
        this.lastMessageId = lastMessageId;
        this.lastMessagePreview = lastMessagePreview;
        this.lastMessageType = lastMessageType;
        this.lastMessageSenderId = lastMessageSenderId;
        this.lastMessageAt = lastMessageAt;
        this.archived = archived;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Conversation start(
            String conversationId,
            String buyerId,
            String buyerDisplayName,
            String buyerAvatarUrl,
            String merchantId,
            String merchantDisplayName,
            String merchantAvatarUrl,
            String startedBy) {
        Guard.require(!buyerId.equals(merchantId),
                () -> new ChatException(ChatErrorCode.INVALID_ARGUMENT, "buyerId must differ from merchantId"));
        Instant now = Instant.now();
        Participant buyer = new Participant(buyerId, ParticipantRole.BUYER,
                buyerDisplayName, buyerAvatarUrl, now, null);
        Participant merchant = new Participant(merchantId, ParticipantRole.MERCHANT,
                merchantDisplayName, merchantAvatarUrl, now, null);
        Conversation c = new Conversation(conversationId, buyerId, merchantId,
                List.of(buyer, merchant), null, null, null, null, null, false, now, now);
        c.record(new ChatEvents.ConversationStarted(conversationId,
                List.of(buyerId, merchantId), startedBy, now));
        return c;
    }

    public Participant requireParticipant(String userId) {
        return participants.stream()
                .filter(p -> p.userId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ChatException(ChatErrorCode.CONVERSATION_FORBIDDEN));
    }

    public List<String> recipientsExcept(String senderId) {
        return participants.stream()
                .map(Participant::userId)
                .filter(id -> !id.equals(senderId))
                .toList();
    }

    public void joinSupport(String supportUserId, String displayName, String avatarUrl) {
        Optional<Participant> existing = participants.stream()
                .filter(p -> p.userId().equals(supportUserId))
                .findFirst();
        if (existing.isPresent())
            return;
        participants.add(new Participant(supportUserId, ParticipantRole.SUPPORT,
                displayName, avatarUrl, Instant.now(), null));
        touch();
    }

    public void recordMessageSent(String messageId, MessageType type, String preview, String senderId) {
        requireParticipant(senderId);
        if (archived) {
            // unarchive on first send so re-engagement re-opens the thread
            this.archived = false;
        }
        this.lastMessageId = messageId;
        this.lastMessageType = type;
        this.lastMessagePreview = preview;
        this.lastMessageSenderId = senderId;
        Instant now = Instant.now();
        this.lastMessageAt = now;
        this.updatedAt = now;
    }

    public void markRead(String userId) {
        Participant participant = requireParticipant(userId);
        Instant now = Instant.now();
        replaceParticipant(participant.withLastReadAt(now));
        this.updatedAt = now;
        record(new ChatEvents.ConversationRead(conversationId, userId, now, now));
    }

    public int unreadCountFor(String userId, long totalMessages, long messagesByOther) {
        Participant participant = requireParticipant(userId);
        Instant lastRead = participant.lastReadAt();
        if (lastMessageAt == null)
            return 0;
        if (lastRead != null && !lastRead.isBefore(lastMessageAt))
            return 0;
        // Unread is messages-not-by-me-after-lastReadAt; the repository computes
        // it precisely. The aggregate just exposes a coarse estimate so the
        // service can short-circuit when there is nothing new.
        return (int) Math.max(0, messagesByOther);
    }

    public void archive(String userId) {
        requireParticipant(userId);
        this.archived = true;
        this.updatedAt = Instant.now();
    }

    public void unarchive(String userId) {
        requireParticipant(userId);
        this.archived = false;
        this.updatedAt = Instant.now();
    }

    public Map<String, Instant> participantLastReadMap() {
        Map<String, Instant> map = new LinkedHashMap<>();
        for (Participant p : participants) {
            map.put(p.userId(), p.lastReadAt());
        }
        return map;
    }

    public List<Participant> participants() {
        return List.copyOf(participants);
    }

    private void replaceParticipant(Participant updated) {
        for (int i = 0; i < participants.size(); i++) {
            if (participants.get(i).userId().equals(updated.userId())) {
                participants.set(i, updated);
                return;
            }
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @Override
    protected String aggregateId() {
        return conversationId;
    }
}
