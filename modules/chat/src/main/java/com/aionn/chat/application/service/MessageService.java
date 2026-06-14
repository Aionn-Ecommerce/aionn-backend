package com.aionn.chat.application.service;

import com.aionn.chat.application.dto.message.command.MessageCommands;
import com.aionn.chat.application.dto.message.result.MessageResult;
import com.aionn.chat.application.mapper.ChatResultMapper;
import com.aionn.chat.application.port.out.ConversationPersistencePort;
import com.aionn.chat.application.port.out.MerchantAutoReplyPersistencePort;
import com.aionn.chat.application.port.out.MessagePersistencePort;
import com.aionn.chat.application.port.out.PresenceTracker;
import com.aionn.chat.application.port.out.RealtimeBroadcaster;
import com.aionn.chat.application.port.out.UserBlockPersistencePort;
import com.aionn.chat.application.port.out.integration.ChatIntegrationEventPublisherPort;
import com.aionn.chat.domain.exception.ChatErrorCode;
import com.aionn.chat.domain.exception.ChatException;
import com.aionn.chat.domain.model.Conversation;
import com.aionn.chat.domain.model.MerchantAutoReply;
import com.aionn.chat.domain.model.Message;
import com.aionn.chat.domain.valueobject.MessagePayload;
import com.aionn.chat.domain.valueobject.MessageType;
import com.aionn.chat.domain.valueobject.Participant;
import com.aionn.chat.domain.valueobject.ParticipantRole;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private static final String DEFAULT_AWAY_MESSAGE = "Hiện tại shop đang ngoài giờ làm việc, sẽ phản hồi bạn sớm nhất.";

    private final ConversationPersistencePort conversationRepository;
    private final MessagePersistencePort messageRepository;
    private final UserBlockPersistencePort userBlockRepository;
    private final MerchantAutoReplyPersistencePort autoReplyRepository;
    private final ChatResultMapper mapper;
    private final EventPublisher eventPublisher;
    private final RealtimeBroadcaster broadcaster;
    private final PresenceTracker presenceTracker;
    private final ChatIntegrationEventPublisherPort integrationEventPublisher;

    public MessageResult send(MessageCommands.SendMessage command) {
        Conversation conversation = conversationRepository.findById(command.conversationId())
                .orElseThrow(() -> new ChatException(ChatErrorCode.CONVERSATION_NOT_FOUND));
        Participant sender = conversation.requireParticipant(command.senderId());

        // Refuse if either side has blocked the other.
        for (String recipientId : conversation.recipientsExcept(command.senderId())) {
            if (userBlockRepository.exists(recipientId, command.senderId())
                    || userBlockRepository.exists(command.senderId(), recipientId)) {
                throw new ChatException(ChatErrorCode.USER_BLOCKED);
            }
        }

        MessagePayload payload = buildPayload(command);
        List<String> recipients = conversation.recipientsExcept(command.senderId());
        Message message = Message.send(IdGenerator.ulid(), conversation.getConversationId(),
                command.senderId(), sender.role(), command.type(), payload, recipients);
        conversation.recordMessageSent(message.getMessageId(), command.type(),
                message.previewBody(), command.senderId());

        Message savedMessage = messageRepository.save(message);
        Conversation savedConversation = conversationRepository.save(conversation);
        eventPublisher.publish(message.pullEvents());
        eventPublisher.publish(conversation.pullEvents());

        MessageResult result = mapper.toResult(savedMessage);
        broadcaster.broadcastMessage(result, recipients);

        // Fan-out fallback notification for offline recipients only - online
        // ones already received the STOMP frame above.
        Set<String> onlineRecipients = presenceTracker.filterOnline(new HashSet<>(recipients));
        String senderDisplayName = sender.displayName() == null ? sender.userId() : sender.displayName();
        for (String recipientId : recipients) {
            if (!onlineRecipients.contains(recipientId)) {
                integrationEventPublisher.publishMessageSent(
                        conversation.getConversationId(),
                        savedMessage.getMessageId(),
                        command.senderId(),
                        recipientId,
                        senderDisplayName,
                        savedMessage.previewBody());
            }
        }
        triggerAutoReplyIfApplicable(savedConversation, sender, savedMessage);
        return result;
    }

    public MessageResult markDelivered(MessageCommands.DeliverMessage command) {
        Message m = required(command.messageId());
        ensureCallerIsParticipant(m, command.userId());
        m.markDeliveredTo(command.userId());
        Message saved = messageRepository.save(m);
        eventPublisher.publish(m.pullEvents());
        return mapper.toResult(saved);
    }

    public MessageResult markRead(MessageCommands.ReadMessage command) {
        Message m = required(command.messageId());
        ensureCallerIsParticipant(m, command.userId());
        m.markReadBy(command.userId());
        Message saved = messageRepository.save(m);
        eventPublisher.publish(m.pullEvents());
        return mapper.toResult(saved);
    }

    public MessageResult recall(MessageCommands.RecallMessage command) {
        Message m = required(command.messageId());
        ensureCallerIsParticipant(m, command.userId());
        m.recall(command.userId());
        Message saved = messageRepository.save(m);
        eventPublisher.publish(m.pullEvents());
        broadcaster.broadcastMessageRecalled(m.getConversationId(), m.getMessageId());
        return mapper.toResult(saved);
    }

    public void setTyping(MessageCommands.SetTyping command) {
        Conversation conversation = conversationRepository.findById(command.conversationId())
                .orElseThrow(() -> new ChatException(ChatErrorCode.CONVERSATION_NOT_FOUND));
        conversation.requireParticipant(command.userId());
        broadcaster.broadcastTypingChange(command.conversationId(), command.userId(), command.typing());
    }

    @Transactional(readOnly = true)
    public List<MessageResult> listLatest(String userId, String conversationId, int limit) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CONVERSATION_NOT_FOUND));
        conversation.requireParticipant(userId);
        return messageRepository.findByConversationLatest(conversationId, limit).stream()
                .map(mapper::toResult)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MessageResult> listBefore(String userId, String conversationId, Instant before, int limit) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CONVERSATION_NOT_FOUND));
        conversation.requireParticipant(userId);
        return messageRepository.findByConversationBefore(conversationId, before, limit).stream()
                .map(mapper::toResult)
                .toList();
    }

    private static MessagePayload buildPayload(MessageCommands.SendMessage command) {
        return switch (command.type()) {
            case TEXT -> MessagePayload.text(command.body());
            case SYSTEM -> MessagePayload.system(command.body());
            default -> new MessagePayload(command.body(),
                    command.metadata() == null ? java.util.Map.of() : command.metadata());
        };
    }

    private Message required(String messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.MESSAGE_NOT_FOUND));
    }

    private void ensureCallerIsParticipant(Message message, String userId) {
        Conversation conversation = conversationRepository.findById(message.getConversationId())
                .orElseThrow(() -> new ChatException(ChatErrorCode.CONVERSATION_NOT_FOUND));
        conversation.requireParticipant(userId);
    }

    private void triggerAutoReplyIfApplicable(Conversation conversation, Participant sender, Message original) {
        if (sender.role() != ParticipantRole.BUYER)
            return;
        var autoReplyOpt = autoReplyRepository.findByMerchantId(conversation.getMerchantId());
        if (autoReplyOpt.isEmpty())
            return;
        MerchantAutoReply autoReply = autoReplyOpt.get();
        if (!autoReply.isEnabled())
            return;
        if (autoReply.isWithinWorkingHours(Instant.now()))
            return;
        String replyBody = autoReply.getAwayMessage() == null
                ? DEFAULT_AWAY_MESSAGE
                : autoReply.getAwayMessage();
        Participant merchantParticipant = conversation.requireParticipant(conversation.getMerchantId());
        Message reply = Message.send(IdGenerator.ulid(), conversation.getConversationId(),
                conversation.getMerchantId(), merchantParticipant.role(), MessageType.SYSTEM,
                MessagePayload.system(replyBody),
                conversation.recipientsExcept(conversation.getMerchantId()));
        conversation.recordMessageSent(reply.getMessageId(), MessageType.SYSTEM,
                reply.previewBody(), conversation.getMerchantId());
        Message savedReply = messageRepository.save(reply);
        conversationRepository.save(conversation);
        eventPublisher.publish(reply.pullEvents());
        eventPublisher.publish(conversation.pullEvents());
        broadcaster.broadcastMessage(mapper.toResult(savedReply),
                conversation.recipientsExcept(conversation.getMerchantId()));
    }
}
