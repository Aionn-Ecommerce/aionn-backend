package com.aionn.chat.adapter.rest.controller;

import com.aionn.chat.adapter.rest.exception.ChatExceptionHandler;
import com.aionn.chat.adapter.rest.support.MockAuthenticationArgumentResolver;
import com.aionn.chat.adapter.rest.support.MockSecurityInterceptor;
import com.aionn.chat.application.dto.block.command.BlockCommands;
import com.aionn.chat.application.dto.block.result.BlockResult;
import com.aionn.chat.application.service.UserBlockService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserBlockControllerWebTest {

    @Mock
    private UserBlockService blockService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserBlockController controller = new UserBlockController(blockService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ChatExceptionHandler())
                .addInterceptors(new MockSecurityInterceptor())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(
                        Jackson2ObjectMapperBuilder.json().build()))
                .setCustomArgumentResolvers(new MockAuthenticationArgumentResolver())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "blocker-1", "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static BlockResult sample(String blockedId, boolean active) {
        Instant now = Instant.now();
        return new BlockResult("blk-1", "blocker-1", blockedId, "spam", active, now, now);
    }

    @Test
    void blockUserPersistsAndReturnsResult() throws Exception {
        when(blockService.block(any(BlockCommands.BlockUser.class)))
                .thenReturn(sample("user-9", true));

        mockMvc.perform(post("/api/v1/chat/blocks")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "blockedId": "user-9",
                                  "reason": "spam"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blockedId").value("user-9"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.message").value("User blocked"));

        verify(blockService).block(any(BlockCommands.BlockUser.class));
    }

    @Test
    void blockSelfReturnsBadRequest() throws Exception {
        when(blockService.block(any(BlockCommands.BlockUser.class)))
                .thenThrow(new ChatException(ChatErrorCode.BLOCK_SELF));

        mockMvc.perform(post("/api/v1/chat/blocks")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "blockedId": "blocker-1"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value(ChatErrorCode.BLOCK_SELF.getCode()));
    }

    @Test
    void blockRejectsBlankBlockedId() throws Exception {
        mockMvc.perform(post("/api/v1/chat/blocks")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "blockedId": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unblockUserReturnsResult() throws Exception {
        when(blockService.unblock(any(BlockCommands.UnblockUser.class)))
                .thenReturn(sample("user-9", false));

        mockMvc.perform(delete("/api/v1/chat/blocks/user-9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blockedId").value("user-9"))
                .andExpect(jsonPath("$.data.active").value(false))
                .andExpect(jsonPath("$.message").value("User unblocked"));
    }

    @Test
    void listMyBlocksReturnsBlocks() throws Exception {
        when(blockService.listMyBlocks("blocker-1"))
                .thenReturn(List.of(sample("user-1", true), sample("user-2", true)));

        mockMvc.perform(get("/api/v1/chat/blocks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].blockedId").value("user-1"))
                .andExpect(jsonPath("$.data[1].blockedId").value("user-2"));
    }
}
