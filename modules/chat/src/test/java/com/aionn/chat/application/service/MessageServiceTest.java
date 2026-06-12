package com.aionn.chat.application.service;

import com.aionn.chat.application.dto.message.command.MessageCommands;
import com.aionn.chat.application.dto.message.result.MessageResult;
import com.aionn.chat.application.mapper.ChatResultMapper;
import com.aionn.chat.application.port.out.ConversationRepository;
import com.aionn.chat.application.port.out.MerchantAutoReplyRepository;
import com.aionn.chat.application.port.out.MessageRepository;
import com.aionn.chat.application.port.out.PresenceTracker;
import com.aionn.chat.application.port.out.RealtimeBroadcaster;
import com.aionn.chat.application.port.out.UserBlockRepository;
import com.aionn.chat.application.port.out.integration.ChatIntegrationEventPublisherPort;
import com.aionn.chat.domain.exception.ChatException;
import com.aionn.chat.domain.model.Conversation;
import com.aionn.chat.domain.model.Message;
import com.aionn.chat.domain.valueobject.MessagePayload;
import com.aionn.chat.domain.valueobject.MessageStatus;
import com.aionn.chat.domain.valueobject.MessageType;
import com.aionn.chat.domain.valueobject.ParticipantRole;
import com.aionn.sharedkernel.application.port.EventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the H3 participant-ACL fix on markDelivered/markRead/recall and the
 * USER_BLOCKED guard in send. Integration event fan-out is verified to fire
 * only for offline recipients.
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    ConversationRepository conversationRepository;
    @Mock
    MessageRepository messageRepository;
    @Mock
    UserBlockRepository userBlockRepository;
    @Mock
    MerchantAutoReplyRepository autoReplyRepository;
    @Mock
    ChatResultMapper mapper;
    @Mock
    EventPublisher eventPublisher;
    @Mock
    RealtimeBroadcaster broadcaster;
    @Mock
    PresenceTracker presenceTracker;
    @Mock
    ChatIntegrationEventPublisherPort integrationEventPublisher;

    @InjectMocks
    MessageService messageService;

    @Test
    void send_rejectsWhenSenderNotParticipant() {
        Conversation c = buyerMerchantConversation();
        when(conversationRepository.findById("C_1")).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> messageService.send(new MessageCommands.SendMessage(
                "U_eavesdropper", "C_1", MessageType.TEXT, "hi", null)))
                .isInstanceOf(ChatException.class)
                .hasFieldOrPropertyWithValue("errorCode", "CHT_002");

        verify(messageRepository, never()).save(any());
    }

    @Test
    void send_rejectsWhenRecipientHasBlockedSender() {
        Conversation c = buyerMerchantConversation();
        when(conversationRepository.findById("C_1")).thenReturn(Optional.of(c));
        when(userBlockRepository.exists("M_seller", "U_buyer")).thenReturn(true);

        assertThatThrownBy(() -> messageService.send(new MessageCommands.SendMessage(
                "U_buyer", "C_1", MessageType.TEXT, "hi", null)))
                .isInstanceOf(ChatException.class)
                .hasFieldOrPropertyWithValue("errorCode", "CHT_201");

        verify(messageRepository, never()).save(any());
    }

    @Test
    void send_publishesIntegrationEventOnlyForOfflineRecipients() {
        Conversation c = buyerMerchantConversation();
        when(conversationRepository.findById("C_1")).thenReturn(Optional.of(c));
        when(userBlockRepository.exists(anyString(), anyString())).thenReturn(false);
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(conversationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(presenceTracker.filterOnline(any())).thenReturn(Set.of()); // all offline
        when(autoReplyRepository.findByMerchantId(anyString())).thenReturn(Optional.empty());
        when(mapper.toResult(any(Message.class))).thenReturn(stubResult());

        messageService.send(new MessageCommands.SendMessage(
                "U_buyer", "C_1", MessageType.TEXT, "hi", null));

        verify(integrationEventPublisher).publishMessageSent(
                eq("C_1"), anyString(), eq("U_buyer"), eq("M_seller"),
                eq("Buyer"), eq("hi"));
    }

    @Test
    void send_skipsIntegrationEventForOnlineRecipients() {
        Conversation c = buyerMerchantConversation();
        when(conversationRepository.findById("C_1")).thenReturn(Optional.of(c));
        when(userBlockRepository.exists(anyString(), anyString())).thenReturn(false);
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(conversationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(presenceTracker.filterOnline(any())).thenReturn(Set.of("M_seller"));
        when(autoReplyRepository.findByMerchantId(anyString())).thenReturn(Optional.empty());
        when(mapper.toResult(any(Message.class))).thenReturn(stubResult());

        messageService.send(new MessageCommands.SendMessage(
                "U_buyer", "C_1", MessageType.TEXT, "hi", null));

        verify(integrationEventPublisher, never())
                .publishMessageSent(any(), any(), any(), any(), any(), any());
    }

    @Test
    void markDelivered_rejectsCallerNotInConversation() {
        Conversation c = buyerMerchantConversation();
        Message msg = sentMessage();
        when(messageRepository.findById("M_1")).thenReturn(Optional.of(msg));
        when(conversationRepository.findById("C_1")).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> messageService.markDelivered(
                new MessageCommands.DeliverMessage("U_eavesdropper", "M_1")))
                .isInstanceOf(ChatException.class)
                .hasFieldOrPropertyWithValue("errorCode", "CHT_002");

        verify(messageRepository, never()).save(any());
    }

    @Test
    void markRead_rejectsCallerNotInConversation() {
        Conversation c = buyerMerchantConversation();
        Message msg = sentMessage();
        when(messageRepository.findById("M_1")).thenReturn(Optional.of(msg));
        when(conversationRepository.findById("C_1")).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> messageService.markRead(
                new MessageCommands.ReadMessage("U_eavesdropper", "M_1")))
                .isInstanceOf(ChatException.class)
                .hasFieldOrPropertyWithValue("errorCode", "CHT_002");

        verify(messageRepository, never()).save(any());
    }

    @Test
    void recall_rejectsCallerNotInConversation() {
        Conversation c = buyerMerchantConversation();
        Message msg = sentMessage();
        when(messageRepository.findById("M_1")).thenReturn(Optional.of(msg));
        when(conversationRepository.findById("C_1")).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> messageService.recall(
                new MessageCommands.RecallMessage("U_eavesdropper", "M_1")))
                .isInstanceOf(ChatException.class)
                .hasFieldOrPropertyWithValue("errorCode", "CHT_002");

        verify(broadcaster, never()).broadcastMessageRecalled(any(), any());
    }

    @Test
    void markRead_byParticipant_passesAndPersists() {
        Conversation c = buyerMerchantConversation();
        Message msg = sentMessage();
        when(messageRepository.findById("M_1")).thenReturn(Optional.of(msg));
        when(conversationRepository.findById("C_1")).thenReturn(Optional.of(c));
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResult(any(Message.class))).thenReturn(stubResult());

        messageService.markRead(new MessageCommands.ReadMessage("M_seller", "M_1"));

        assertThat(msg.getReadBy()).contains("M_seller");
        verify(messageRepository).save(msg);
    }

    private static Conversation buyerMerchantConversation() {
        return Conversation.start("C_1",
                "U_buyer", "Buyer", null,
                "M_seller", "Seller", null,
                "U_buyer");
    }

    private static Message sentMessage() {
        return new Message("M_1", "C_1", "U_buyer", ParticipantRole.BUYER, MessageType.TEXT,
                MessagePayload.text("hi"), MessageStatus.SENT,
                new HashSet<>(), new HashSet<>(), false,
                java.time.Instant.now(), java.time.Instant.now());
    }

    private static MessageResult stubResult() {
        return new MessageResult("M_1", "C_1", "U_buyer", "BUYER", "TEXT",
                "hi", java.util.Map.of(), "SENT",
                Set.of(), Set.of(), false,
                java.time.Instant.now(), java.time.Instant.now());
    }
}
