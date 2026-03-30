package com.ecommerce.identity.adapter.rest.controller;

import com.ecommerce.identity.application.service.IdentityAgentService;
import com.ecommerce.identity.infrastructure.persistence.entity.AgentIdentityEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.SecurityAuditEntity;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AgentIdentityControllerTest {

    @Mock
    private IdentityAgentService agentService;

    @InjectMocks
    private AgentIdentityController agentIdentityController;

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(agentIdentityController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    private Authentication auth() {
        return new UsernamePasswordAuthenticationToken("user-1", "N/A");
    }

    private AgentIdentityEntity agent(String status) {
        return AgentIdentityEntity.builder()
                .agentId("agent-1")
                .keyHash("key-1")
                .permissions("{\"scope\":\"basic\"}")
                .status(status)
                .expiryAt(LocalDateTime.of(2027, 3, 20, 10, 0))
                .createdAt(LocalDateTime.of(2026, 3, 20, 10, 0))
                .build();
    }

    @Test
    void createShouldReturnSuccess() throws Exception {
        Mockito.when(agentService.create("user-1")).thenReturn(agent("ACTIVE"));

        mockMvc().perform(post("/api/v1/agent-identities")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"agentName":"bot-1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"agentId\":\"agent-1\"")));
    }

    @Test
    void updatePermissionsShouldReturnSuccess() throws Exception {
        Mockito.when(agentService.updatePermissions("user-1", "agent-1", "{\"scope\":\"order:read\"}"))
                .thenReturn(agent("ACTIVE"));

        mockMvc().perform(put("/api/v1/agent-identities/agent-1/permissions")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"permissionsJson":"{\\"scope\\":\\"order:read\\"}"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Agent permissions updated")));
    }

    @Test
    void suspendShouldReturnSuccess() throws Exception {
        Mockito.when(agentService.suspend("user-1", "agent-1")).thenReturn(agent("SUSPENDED"));

        mockMvc().perform(post("/api/v1/agent-identities/agent-1/suspend").principal(auth()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"status\":\"SUSPENDED\"")));
    }

    @Test
    void auditLogsShouldReturnSuccess() throws Exception {
        SecurityAuditEntity audit = SecurityAuditEntity.builder()
                .auditId("audit-1")
                .eventType("AGENT_SUSPENDED")
                .description("Agent suspended: agent-1")
                .timestamp(LocalDateTime.of(2026, 3, 20, 10, 0))
                .build();
        Mockito.when(agentService.getAgentAuditLogs("user-1", "agent-1")).thenReturn(List.of(audit));

        mockMvc().perform(get("/api/v1/agent-identities/agent-1/audit-logs").principal(auth()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"auditId\":\"audit-1\"")));
    }

    @Test
    void revokeShouldReturnSuccess() throws Exception {
        mockMvc().perform(delete("/api/v1/agent-identities/agent-1").principal(auth()))
                .andExpect(status().isNoContent());
    }
}
