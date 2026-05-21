package com.aionn.chat.application.mapper;

import com.aionn.chat.application.dto.autoreply.result.AutoReplyResult;
import com.aionn.chat.application.dto.block.result.BlockResult;
import com.aionn.chat.application.dto.conversation.result.ConversationResult;
import com.aionn.chat.application.dto.message.result.MessageResult;
import com.aionn.chat.domain.model.Conversation;
import com.aionn.chat.domain.model.MerchantAutoReply;
import com.aionn.chat.domain.model.Message;
import com.aionn.chat.domain.model.UserBlock;
import org.springframework.stereotype.Component;

@Component
public class ChatResultMapper {

    public ConversationResult toResult(Conversation c, long unreadCount) {
        return new ConversationResult(
                c.getConversationId(),
                c.getBuyerId(),
                c.getMerchantId(),
                c.participants().stream()
                        .map(p -> new ConversationResult.ParticipantResult(
                                p.userId(), p.role().name(), p.displayName(), p.avatarUrl(),
                                p.joinedAt(), p.lastReadAt()))
                        .toList(),
                c.getLastMessageId(),
                c.getLastMessagePreview(),
                c.getLastMessageType() == null ? null : c.getLastMessageType().name(),
                c.getLastMessageSenderId(),
                c.getLastMessageAt(),
                c.isArchived(),
                unreadCount,
                c.getCreatedAt(),
                c.getUpdatedAt());
    }

    public MessageResult toResult(Message m) {
        return new MessageResult(
                m.getMessageId(),
                m.getConversationId(),
                m.getSenderId(),
                m.getSenderRole().name(),
                m.getType().name(),
                m.getPayload().body(),
                m.getPayload().metadata(),
                m.getStatus().name(),
                java.util.Set.copyOf(m.getDeliveredTo()),
                java.util.Set.copyOf(m.getReadBy()),
                m.isRecalled(),
                m.getSentAt(),
                m.getUpdatedAt());
    }

    public BlockResult toResult(UserBlock b) {
        return new BlockResult(
                b.getBlockId(), b.getBlockerId(), b.getBlockedId(), b.getReason(),
                b.isActive(), b.getCreatedAt(), b.getUpdatedAt());
    }

    public AutoReplyResult toResult(MerchantAutoReply a) {
        return new AutoReplyResult(
                a.getMerchantId(),
                a.isEnabled(),
                a.getGreeting(),
                a.getAwayMessage(),
                a.getWorkingHourStart(),
                a.getWorkingHourEnd(),
                a.getWorkingDays(),
                a.getTimezone() == null ? null : a.getTimezone().getId(),
                a.getCreatedAt(),
                a.getUpdatedAt());
    }
}

