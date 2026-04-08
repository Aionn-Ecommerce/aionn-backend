package com.ecommerce.identity.adapter.rest.controller;

import com.ecommerce.sharedkernel.infrastructure.web.ClientIpResolver;
import com.ecommerce.sharedkernel.adapter.web.support.ClientIpArgumentResolver;
import com.ecommerce.identity.application.service.AdminUserService;
import com.ecommerce.identity.application.service.SecurityService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SecurityControllerTest {

    @Mock
    private SecurityService securityService;

    @Mock
    private AdminUserService adminUserService;

    @Mock
    private ClientIpResolver clientIpResolver;

    @InjectMocks
    private SecurityController securityController;

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(securityController)
                .setCustomArgumentResolvers(new ClientIpArgumentResolver(clientIpResolver))
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    private Authentication auth() {
        return new UsernamePasswordAuthenticationToken("user-1", "N/A");
    }

    @Test
    void changePasswordShouldReturnSuccess() throws Exception {
        Mockito.when(clientIpResolver.resolve(Mockito.any())).thenReturn("127.0.0.1");

        mockMvc().perform(put("/api/v1/security/password")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"old","newPassword":"new"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Password changed")));
    }

    @Test
    void requestPasswordResetShouldReturnToken() throws Exception {
        Mockito.when(clientIpResolver.resolve(Mockito.any())).thenReturn("127.0.0.1");
        Mockito.when(securityService.requestPasswordReset("john@example.com", "127.0.0.1"))
                .thenReturn("reset-token-1");

        mockMvc().perform(post("/api/v1/security/password-reset-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identity":"john@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("reset-token-1")));
    }

    @Test
    void completePasswordResetShouldReturnSuccess() throws Exception {
        Mockito.when(clientIpResolver.resolve(Mockito.any())).thenReturn("127.0.0.1");

        mockMvc().perform(post("/api/v1/security/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"reset-token-1","newPassword":"Password@123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Password reset completed")));
    }

    @Test
    void enableMfaShouldReturnEnabled() throws Exception {
        Mockito.when(clientIpResolver.resolve(Mockito.any())).thenReturn("127.0.0.1");
        Mockito.when(securityService.enableMfa("user-1", "Password@123", "127.0.0.1")).thenReturn(true);

        mockMvc().perform(post("/api/v1/security/mfa/enable")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password":"Password@123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"mfaEnabled\":true")));
    }

    @Test
    void disableMfaShouldReturnDisabled() throws Exception {
        Mockito.when(clientIpResolver.resolve(Mockito.any())).thenReturn("127.0.0.1");
        Mockito.when(securityService.disableMfa("user-1", "Password@123", "127.0.0.1")).thenReturn(false);

        mockMvc().perform(post("/api/v1/security/mfa/disable")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password":"Password@123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"mfaEnabled\":false")));
    }

    @Test
    void backupCodesShouldReturnList() throws Exception {
        Mockito.when(clientIpResolver.resolve(Mockito.any())).thenReturn("127.0.0.1");
        Mockito.when(securityService.regenerateBackupCodes("user-1", "127.0.0.1"))
                .thenReturn(List.of("11111111", "22222222"));

        mockMvc().perform(post("/api/v1/security/mfa/backup-codes").principal(auth()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("11111111")));
    }

    @Test
    void auditLogsShouldReturnList() throws Exception {
        SecurityAuditEntity log = SecurityAuditEntity.builder()
                .auditId("audit-1")
                .eventType("LOGIN")
                .description("New login")
                .ipAddress("127.0.0.1")
                .deviceId("device-1")
                .timestamp(LocalDateTime.of(2026, 3, 20, 10, 0))
                .build();
        Mockito.when(securityService.getAuditLogs("user-1")).thenReturn(List.of(log));

        mockMvc().perform(get("/api/v1/security/audit-logs").principal(auth()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"auditId\":\"audit-1\"")))
                .andExpect(content().string(Matchers.containsString("\"eventType\":\"LOGIN\"")));
    }

    @Test
    void unlockAccountShouldReturnSuccess() throws Exception {
        mockMvc().perform(post("/api/v1/admin/security/unlock-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":"user-1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Account unlocked")));
    }
}


