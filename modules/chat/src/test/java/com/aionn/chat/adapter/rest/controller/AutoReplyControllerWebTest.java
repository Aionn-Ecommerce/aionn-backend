package com.aionn.chat.adapter.rest.controller;

import com.aionn.chat.adapter.rest.exception.ChatExceptionHandler;
import com.aionn.chat.adapter.rest.support.MockAuthenticationArgumentResolver;
import com.aionn.chat.adapter.rest.support.MockSecurityInterceptor;
import com.aionn.chat.application.dto.autoreply.command.AutoReplyCommands;
import com.aionn.chat.application.dto.autoreply.result.AutoReplyResult;
import com.aionn.chat.application.service.AutoReplyService;
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

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AutoReplyControllerWebTest {

    @Mock
    private AutoReplyService autoReplyService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AutoReplyController controller = new AutoReplyController(autoReplyService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ChatExceptionHandler())
                .addInterceptors(new MockSecurityInterceptor())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(
                        Jackson2ObjectMapperBuilder.json().build()))
                .setCustomArgumentResolvers(new MockAuthenticationArgumentResolver())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "owner-1", "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static AutoReplyResult sample(String merchantId) {
        Instant now = Instant.now();
        return new AutoReplyResult(merchantId, true, "Hi!", "Away", LocalTime.of(9, 0),
                LocalTime.of(17, 0), EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                "Asia/Ho_Chi_Minh", now, now);
    }

    @Test
    void getReturnsAutoReply() throws Exception {
        when(autoReplyService.get("owner-1", "m-1")).thenReturn(sample("m-1"));

        mockMvc.perform(get("/api/v1/chat/merchants/m-1/auto-reply"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.merchantId").value("m-1"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.greeting").value("Hi!"));

        verify(autoReplyService).get("owner-1", "m-1");
    }

    @Test
    void updatePersistsConfig() throws Exception {
        when(autoReplyService.update(any(AutoReplyCommands.UpdateAutoReply.class)))
                .thenReturn(sample("m-1"));

        mockMvc.perform(put("/api/v1/chat/merchants/m-1/auto-reply")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "enabled": true,
                                  "greeting": "Hi!",
                                  "awayMessage": "Away",
                                  "workingHourStart": "09:00",
                                  "workingHourEnd": "17:00",
                                  "workingDays": ["MONDAY","TUESDAY"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.merchantId").value("m-1"))
                .andExpect(jsonPath("$.message").value("Auto-reply config saved"));

        verify(autoReplyService).update(any(AutoReplyCommands.UpdateAutoReply.class));
    }

    @Test
    void getForbiddenWhenNotMerchantOwner() throws Exception {
        when(autoReplyService.get("owner-1", "m-1"))
                .thenThrow(new ChatException(ChatErrorCode.AUTO_REPLY_FORBIDDEN));

        mockMvc.perform(get("/api/v1/chat/merchants/m-1/auto-reply"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.errorCode").value(ChatErrorCode.AUTO_REPLY_FORBIDDEN.getCode()));
    }
}
