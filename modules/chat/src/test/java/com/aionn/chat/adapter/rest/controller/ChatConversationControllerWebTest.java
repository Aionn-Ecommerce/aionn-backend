package com.aionn.chat.adapter.rest.controller;

import com.aionn.chat.adapter.rest.exception.ChatExceptionHandler;
import com.aionn.chat.adapter.rest.support.MockAuthenticationArgumentResolver;
import com.aionn.chat.adapter.rest.support.MockSecurityInterceptor;
import com.aionn.chat.application.dto.conversation.command.ConversationCommands;
import com.aionn.chat.application.dto.conversation.result.ConversationResult;
import com.aionn.chat.application.service.ConversationService;
import com.aionn.chat.domain.exception.ChatErrorCode;
import com.aionn.chat.domain.exception.ChatException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChatConversationControllerWebTest {

    @Mock
    private ConversationService conversationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ChatConversationController controller = new ChatConversationController(conversationService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ChatExceptionHandler())
                .addInterceptors(new MockSecurityInterceptor())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(
                        Jackson2ObjectMapperBuilder.json().build()))
                .setCustomArgumentResolvers(new MockAuthenticationArgumentResolver())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "buyer-1", "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static ConversationResult sample(String id, boolean archived) {
        Instant now = Instant.now();
        return new ConversationResult(id, "buyer-1", "m-1",
                List.of(new ConversationResult.ParticipantResult(
                        "buyer-1", "BUYER", "Buyer", null, now, null)),
                null, null, null, null, null, archived, 0L, now, now);
    }

    @Test
    void startCreatesConversation() throws Exception {
        when(conversationService.startOrGet(any(ConversationCommands.StartConversation.class)))
                .thenReturn(sample("c-1", false));

        mockMvc.perform(post("/api/v1/chat/conversations")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": "m-1",
                                  "buyerDisplayName": "Buyer One"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conversationId").value("c-1"))
                .andExpect(jsonPath("$.message").value("Conversation ready"));

        verify(conversationService).startOrGet(any(ConversationCommands.StartConversation.class));
    }

    @Test
    void startRejectsBlankMerchantId() throws Exception {
        mockMvc.perform(post("/api/v1/chat/conversations")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listMineReturnsMyConversations() throws Exception {
        when(conversationService.listForUser("buyer-1", false, 50))
                .thenReturn(List.of(sample("c-1", false), sample("c-2", false)));

        mockMvc.perform(get("/api/v1/chat/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].conversationId").value("c-1"))
                .andExpect(jsonPath("$.data[1].conversationId").value("c-2"));
    }

    @Test
    void listMineRespectsLimitBounds() throws Exception {
        when(conversationService.listForUser("buyer-1", true, 100))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/chat/conversations")
                        .param("includeArchived", "true")
                        .param("limit", "1000"))
                .andExpect(status().isOk());

        verify(conversationService).listForUser("buyer-1", true, 100);
    }

    @Test
    void getReturnsConversation() throws Exception {
        when(conversationService.getForUser("buyer-1", "c-9"))
                .thenReturn(sample("c-9", false));

        mockMvc.perform(get("/api/v1/chat/conversations/c-9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conversationId").value("c-9"));
    }

    @Test
    void getNotFoundWhenMissing() throws Exception {
        when(conversationService.getForUser("buyer-1", "missing"))
                .thenThrow(new ChatException(ChatErrorCode.CONVERSATION_NOT_FOUND));

        mockMvc.perform(get("/api/v1/chat/conversations/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.errorCode").value(ChatErrorCode.CONVERSATION_NOT_FOUND.getCode()));
    }

    @Test
    void markReadInvokesService() throws Exception {
        when(conversationService.markRead(any(ConversationCommands.MarkRead.class)))
                .thenReturn(sample("c-1", false));

        mockMvc.perform(post("/api/v1/chat/conversations/c-1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Conversation marked read"));

        verify(conversationService).markRead(any(ConversationCommands.MarkRead.class));
    }

    @Test
    void archiveInvokesService() throws Exception {
        when(conversationService.archive(any(ConversationCommands.Archive.class)))
                .thenReturn(sample("c-1", true));

        mockMvc.perform(post("/api/v1/chat/conversations/c-1/archive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.archived").value(true));
    }

    @Test
    void unreadCountsReturnsMap() throws Exception {
        when(conversationService.getUnreadCounts("buyer-1")).thenReturn(Map.of("c-1", 3L));

        mockMvc.perform(get("/api/v1/chat/conversations/unread-counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data['c-1']").value(3));
    }
}
