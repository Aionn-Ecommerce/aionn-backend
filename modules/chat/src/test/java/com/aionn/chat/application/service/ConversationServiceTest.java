package com.aionn.chat.application.service;

import com.aionn.chat.application.dto.conversation.command.ConversationCommands;
import com.aionn.chat.application.dto.conversation.result.ConversationResult;
import com.aionn.chat.application.mapper.ChatResultMapper;
import com.aionn.chat.application.port.out.ConversationPersistencePort;
import com.aionn.chat.application.port.out.MessagePersistencePort;
import com.aionn.chat.application.port.out.RealtimeBroadcaster;
import com.aionn.chat.domain.exception.ChatException;
import com.aionn.chat.domain.model.Conversation;
import com.aionn.sharedkernel.application.port.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationPersistencePort conversationRepository;
    @Mock
    private MessagePersistencePort messageRepository;
    @Mock
    private ChatResultMapper mapper;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private RealtimeBroadcaster broadcaster;

    private ConversationService service;

    @BeforeEach
    void setUp() {
        service = new ConversationService(conversationRepository, messageRepository,
                mapper, eventPublisher, broadcaster);
    }

    private static ConversationResult resultFor(String id) {
        Instant now = Instant.now();
        return new ConversationResult(id, "buyer-1", "m-1", List.of(),
                null, null, null, null, null, false, 0L, now, now);
    }

    @Test
    void startOrGetReturnsExistingWhenFound() {
        Conversation existing = Conversation.start("c-1", "buyer-1", "Buyer", null,
                "m-1", "Merchant", null, "buyer-1");
        when(conversationRepository.findByBuyerAndMerchant("buyer-1", "m-1"))
                .thenReturn(Optional.of(existing));
        when(messageRepository.countUnread(eq("c-1"), eq("buyer-1"), any())).thenReturn(0L);
        when(mapper.toResult(any(Conversation.class), eq(0L))).thenReturn(resultFor("c-1"));

        ConversationResult result = service.startOrGet(new ConversationCommands.StartConversation(
                "buyer-1", "Buyer", null, "m-1", "Merchant", null, "buyer-1"));

        assertThat(result.conversationId()).isEqualTo("c-1");
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void startOrGetCreatesWhenNoExisting() {
        when(conversationRepository.findByBuyerAndMerchant("buyer-1", "m-1"))
                .thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(messageRepository.countUnread(any(), eq("buyer-1"), any())).thenReturn(0L);
        when(mapper.toResult(any(Conversation.class), eq(0L))).thenReturn(resultFor("c-new"));

        ConversationResult result = service.startOrGet(new ConversationCommands.StartConversation(
                "buyer-1", "Buyer", null, "m-1", "Merchant", null, "buyer-1"));

        assertThat(result.conversationId()).isEqualTo("c-new");
        verify(conversationRepository).save(any(Conversation.class));
        verify(eventPublisher).publish(anyCollection());
    }

    @Test
    void markReadUpdatesAndBroadcasts() {
        Conversation c = Conversation.start("c-2", "buyer-1", "Buyer", null,
                "m-1", "Merchant", null, "buyer-1");
        when(conversationRepository.findById("c-2")).thenReturn(Optional.of(c));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(messageRepository.countUnread(eq("c-2"), eq("buyer-1"), any())).thenReturn(0L);
        when(mapper.toResult(any(Conversation.class), eq(0L))).thenReturn(resultFor("c-2"));

        service.markRead(new ConversationCommands.MarkRead("buyer-1", "c-2"));

        verify(conversationRepository).save(any(Conversation.class));
        verify(broadcaster).broadcastConversationRead(eq("c-2"), eq("buyer-1"), any(Instant.class));
    }

    @Test
    void archiveSetsArchivedFlag() {
        Conversation c = Conversation.start("c-3", "buyer-1", "Buyer", null,
                "m-1", "Merchant", null, "buyer-1");
        when(conversationRepository.findById("c-3")).thenReturn(Optional.of(c));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(messageRepository.countUnread(eq("c-3"), eq("buyer-1"), any())).thenReturn(0L);
        when(mapper.toResult(any(Conversation.class), eq(0L))).thenReturn(resultFor("c-3"));

        service.archive(new ConversationCommands.Archive("buyer-1", "c-3"));

        assertThat(c.isArchived()).isTrue();
    }

    @Test
    void getForUserNotFoundThrows() {
        when(conversationRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getForUser("u1", "missing"))
                .isInstanceOf(ChatException.class)
                .hasFieldOrPropertyWithValue("errorCode", "CHT_001");
    }

    @Test
    void getForUserNonParticipantThrows() {
        Conversation c = Conversation.start("c-5", "buyer-1", "Buyer", null,
                "m-1", "Merchant", null, "buyer-1");
        when(conversationRepository.findById("c-5")).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> service.getForUser("stranger", "c-5"))
                .isInstanceOf(ChatException.class)
                .hasFieldOrPropertyWithValue("errorCode", "CHT_002");
    }

    @Test
    void listForUserDelegatesToRepository() {
        Conversation c1 = Conversation.start("c-1", "buyer-1", "Buyer", null,
                "m-1", "Merchant", null, "buyer-1");
        when(conversationRepository.findByUser("buyer-1", true, 25)).thenReturn(List.of(c1));
        when(messageRepository.countUnread(eq("c-1"), eq("buyer-1"), any())).thenReturn(2L);
        when(mapper.toResult(any(Conversation.class), eq(2L))).thenReturn(resultFor("c-1"));

        List<ConversationResult> results = service.listForUser("buyer-1", true, 25);

        assertThat(results).hasSize(1);
        verify(conversationRepository).findByUser("buyer-1", true, 25);
    }

    @Test
    void getUnreadCountsAggregatesAcrossConversations() {
        Conversation c1 = Conversation.start("c-1", "buyer-1", "B", null, "m-1", "M", null, "buyer-1");
        Conversation c2 = Conversation.start("c-2", "buyer-1", "B", null, "m-2", "M2", null, "buyer-1");
        when(conversationRepository.findByUser("buyer-1", false, 100)).thenReturn(List.of(c1, c2));
        when(messageRepository.countUnread(eq("c-1"), eq("buyer-1"), any())).thenReturn(3L);
        when(messageRepository.countUnread(eq("c-2"), eq("buyer-1"), any())).thenReturn(0L);

        var counts = service.getUnreadCounts("buyer-1");

        assertThat(counts).containsEntry("c-1", 3L).containsEntry("c-2", 0L);
    }
}
