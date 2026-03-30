package com.ecommerce.identity.adapter.rest.controller;

import com.ecommerce.identity.application.service.IdentityKycService;
import com.ecommerce.identity.infrastructure.persistence.entity.KycProfileEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class KycControllerTest {

    @Mock
    private IdentityKycService kycService;

    @InjectMocks
    private KycController kycController;

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(kycController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    private Authentication auth() {
        return new UsernamePasswordAuthenticationToken("user-1", "N/A");
    }

    private KycProfileEntity kyc(String id, String status) {
        UserEntity user = new UserEntity();
        user.setUserId("user-1");

        return KycProfileEntity.builder()
                .kycId(id)
                .user(user)
                .docType("CCCD")
                .blobUrl("https://blob/doc.png")
                .status(status)
                .adminId("admin-1")
                .reason("ok")
                .submittedAt(LocalDateTime.of(2026, 3, 20, 10, 0))
                .approvedAt(LocalDateTime.of(2026, 3, 21, 10, 0))
                .build();
    }

    @Test
    void createKycShouldReturnSuccess() throws Exception {
        Mockito.when(kycService.createKyc("user-1", "CCCD")).thenReturn(kyc("kyc-1", "DRAFT"));

        mockMvc().perform(post("/api/v1/kyc")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"docType":"CCCD"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"kycId\":\"kyc-1\"")));
    }

    @Test
    void uploadDocumentShouldReturnSuccess() throws Exception {
        Mockito.when(kycService.uploadDocument("user-1", "kyc-1", "https://blob/doc.png"))
                .thenReturn(kyc("kyc-1", "DRAFT"));

        mockMvc().perform(post("/api/v1/kyc/kyc-1/documents")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"blobUrl":"https://blob/doc.png"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("KYC document uploaded")));
    }

    @Test
    void submitShouldReturnSuccess() throws Exception {
        Mockito.when(kycService.submit("user-1", "kyc-1")).thenReturn(kyc("kyc-1", "SUBMITTED"));

        mockMvc().perform(post("/api/v1/kyc/kyc-1/submit").principal(auth()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"status\":\"SUBMITTED\"")));
    }

    @Test
    void cancelShouldReturnSuccess() throws Exception {
        mockMvc().perform(delete("/api/v1/kyc/kyc-1").principal(auth()))
                .andExpect(status().isNoContent());
    }

    @Test
    void reviewShouldReturnSuccess() throws Exception {
        Mockito.when(kycService.review("user-1", "kyc-1", "reviewing"))
                .thenReturn(kyc("kyc-1", "IN_REVIEW"));

        mockMvc().perform(post("/api/v1/admin/kyc/kyc-1/review")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"note":"reviewing"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"status\":\"IN_REVIEW\"")));
    }

    @Test
    void approveShouldReturnSuccess() throws Exception {
        Mockito.when(kycService.approve("user-1", "kyc-1")).thenReturn(kyc("kyc-1", "APPROVED"));

        mockMvc().perform(post("/api/v1/admin/kyc/kyc-1/approve").principal(auth()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"status\":\"APPROVED\"")));
    }

    @Test
    void rejectShouldReturnSuccess() throws Exception {
        Mockito.when(kycService.reject("user-1", "kyc-1", "invalid"))
                .thenReturn(kyc("kyc-1", "REJECTED"));

        mockMvc().perform(post("/api/v1/admin/kyc/kyc-1/reject")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"invalid"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"status\":\"REJECTED\"")));
    }
}
