package com.aionn.chat.adapter.rest.controller;

import com.aionn.chat.adapter.rest.exception.ChatExceptionHandler;
import com.aionn.chat.adapter.rest.support.MockAuthenticationArgumentResolver;
import com.aionn.chat.adapter.rest.support.MockSecurityInterceptor;
import com.aionn.chat.application.dto.message.command.MessageCommands;
import com.aionn.chat.application.dto.message.result.MessageResult;
import com.aionn.chat.application.service.MessageService;
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
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChatMessageControllerWebTest {

    @Mock
    private MessageService messageService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ChatMessageController controller = new ChatMessageController(messageService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ChatExceptionHandler())
                .addInterceptors(new MockSecurityInterceptor())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(
                        Jackson2ObjectMapperBuilder.json().build()))
                .setCustomArgumentResolvers(new MockAuthenticationArgumentResolver())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "user-1", "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static MessageResult sample(String id, String body) {
        Instant now = Instant.now();
        return new MessageResult(id, "c-1", "user-1", "BUYER",
                "TEXT", body, Map.of(), "SENT", Set.of(), Set.of(),
                false, now, now);
    }

    @Test
    void sendCreatesMessage() throws Exception {
        when(messageService.send(any(MessageCommands.SendMessage.class)))
                .thenReturn(sample("msg-1", "hi"));

        mockMvc.perform(post("/api/v1/chat/conversations/c-1/messages")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "TEXT",
                                  "body": "hi"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messageId").value("msg-1"))
                .andExpect(jsonPath("$.data.body").value("hi"))
                .andExpect(jsonPath("$.message").value("Message sent"));

        verify(messageService).send(any(MessageCommands.SendMessage.class));
    }

    @Test
    void sendRejectsMissingType() throws Exception {
        mockMvc.perform(post("/api/v1/chat/conversations/c-1/messages")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "body": "hi"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listLatestUsesDefaultLimit() throws Exception {
        when(messageService.listLatest("user-1", "c-1", 30))
                .thenReturn(List.of(sample("msg-1", "hi")));

        mockMvc.perform(get("/api/v1/chat/conversations/c-1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].messageId").value("msg-1"));

        verify(messageService).listLatest("user-1", "c-1", 30);
    }

    @Test
    void listBeforeUsesCursor() throws Exception {
        Instant before = Instant.parse("2024-06-01T00:00:00Z");
        when(messageService.listBefore(eq("user-1"), eq("c-1"), eq(before), eq(20)))
                .thenReturn(List.of(sample("msg-2", "older")));

        mockMvc.perform(get("/api/v1/chat/conversations/c-1/messages")
                        .param("before", "2024-06-01T00:00:00Z")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].messageId").value("msg-2"));

        verify(messageService).listBefore(eq("user-1"), eq("c-1"), eq(before), eq(20));
    }

    @Test
    void markDeliveredInvokesService() throws Exception {
        when(messageService.markDelivered(any(MessageCommands.DeliverMessage.class)))
                .thenReturn(sample("msg-3", "x"));

        mockMvc.perform(post("/api/v1/chat/messages/msg-3/delivered"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Message delivered"));

        verify(messageService).markDelivered(any(MessageCommands.DeliverMessage.class));
    }

    @Test
    void recallReturnsMessage() throws Exception {
        when(messageService.recall(any(MessageCommands.RecallMessage.class)))
                .thenReturn(sample("msg-4", "x"));

        mockMvc.perform(post("/api/v1/chat/messages/msg-4/recall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Message recalled"));
    }

    @Test
    void recallExpiredReturnsBadRequest() throws Exception {
        when(messageService.recall(any(MessageCommands.RecallMessage.class)))
                .thenThrow(new ChatException(ChatErrorCode.MESSAGE_RECALL_WINDOW_EXPIRED));

        mockMvc.perform(post("/api/v1/chat/messages/msg-5/recall"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value(ChatErrorCode.MESSAGE_RECALL_WINDOW_EXPIRED.getCode()));
    }

    @Test
    void setTypingInvokesService() throws Exception {
        mockMvc.perform(post("/api/v1/chat/conversations/c-1/typing")
                        .contentType(APPLICATION_JSON)
                        .content("{\"typing\": true}"))
                .andExpect(status().isOk());

        verify(messageService).setTyping(any(MessageCommands.SetTyping.class));
    }
}
