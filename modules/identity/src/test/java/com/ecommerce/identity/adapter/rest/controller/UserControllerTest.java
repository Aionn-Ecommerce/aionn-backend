package com.ecommerce.identity.adapter.rest.controller;

import com.ecommerce.identity.application.dto.user.DataExportRequestView;
import com.ecommerce.identity.application.dto.user.DeletionRequestView;
import com.ecommerce.identity.application.dto.user.UserProfileView;
import com.ecommerce.identity.application.port.in.user.UserSelfServiceInputPort;
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
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserSelfServiceInputPort userSelfServiceInputPort;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(userController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    private Authentication auth() {
        return new UsernamePasswordAuthenticationToken("user-1", "N/A");
    }

    private UserProfileView profile(String displayName) {
        return new UserProfileView(
                "user-1",
                "john@example.com",
                "0987654321",
                "john",
                displayName,
                "https://cdn/avatar.png",
                Set.of("BUYER"),
                "ACTIVE",
                LocalDateTime.of(2026, 3, 20, 9, 0),
                LocalDateTime.of(2026, 3, 20, 9, 0),
                LocalDateTime.of(2026, 3, 1, 9, 0));
    }

    @Test
    void getMyProfileShouldReturnSuccess() throws Exception {
        Mockito.when(userSelfServiceInputPort.getMyProfile("user-1")).thenReturn(profile("John"));

        mockMvc().perform(get("/api/v1/users/me").principal(auth()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"userId\":\"user-1\"")));
    }

    @Test
    void verifyEmailSendOtpShouldReturnSuccess() throws Exception {
        mockMvc().perform(post("/api/v1/users/me/verify-email")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"action":"SEND_OTP"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("OTP_SENT")));
    }

    @Test
    void verifyEmailConfirmShouldReturnSuccess() throws Exception {
        mockMvc().perform(post("/api/v1/users/me/verify-email")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"action":"CONFIRM_OTP","otpCode":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("EMAIL_VERIFIED")));
    }

    @Test
    void updateDisplayNameShouldReturnProfile() throws Exception {
        Mockito.when(userSelfServiceInputPort.updateDisplayName("user-1", "John Updated"))
                .thenReturn(profile("John Updated"));

        mockMvc().perform(patch("/api/v1/users/me/display-name")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName":"John Updated"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("John Updated")));
    }

    @Test
    void updateAvatarShouldReturnProfile() throws Exception {
        Mockito.when(userSelfServiceInputPort.updateAvatar("user-1", "https://cdn/new-avatar.png"))
                .thenReturn(profile("John"));

        mockMvc().perform(patch("/api/v1/users/me/avatar")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"avatarUrl":"https://cdn/new-avatar.png"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Avatar updated")));
    }

    @Test
    void changeEmailSendOtpShouldReturnSuccess() throws Exception {
        mockMvc().perform(patch("/api/v1/users/me/email")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"action":"SEND_OTP","newEmail":"new@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("OTP_SENT")));
    }

    @Test
    void changeEmailConfirmShouldReturnProfile() throws Exception {
        Mockito.when(userSelfServiceInputPort.confirmEmailChange("user-1", "123456"))
                .thenReturn(profile("John"));

        mockMvc().perform(patch("/api/v1/users/me/email")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"action":"CONFIRM_OTP","otpCode":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Email updated")));
    }

    @Test
    void changePhoneSendOtpShouldReturnSuccess() throws Exception {
        mockMvc().perform(patch("/api/v1/users/me/phone")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"action":"SEND_OTP","newPhone":"0911222333"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("OTP_SENT")));
    }

    @Test
    void changePhoneConfirmShouldReturnProfile() throws Exception {
        Mockito.when(userSelfServiceInputPort.confirmPhoneChange("user-1", "123456"))
                .thenReturn(profile("John"));

        mockMvc().perform(patch("/api/v1/users/me/phone")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"action":"CONFIRM_OTP","otpCode":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Phone updated")));
    }

    @Test
    void requestAccountDeletionShouldReturnCreated() throws Exception {
        Mockito.when(userSelfServiceInputPort.requestAccountDeletion("user-1"))
                .thenReturn(new DeletionRequestView(
                        "del-1",
                        "PENDING",
                        LocalDateTime.of(2026, 3, 20, 10, 0),
                        LocalDateTime.of(2026, 4, 19, 10, 0)));

        mockMvc().perform(post("/api/v1/users/me/deletion-requests").principal(auth()))
                .andExpect(status().isCreated())
                .andExpect(content().string(Matchers.containsString("\"statusCode\":\"201\"")))
                .andExpect(content().string(Matchers.containsString("del-1")));
    }

    @Test
    void cancelAccountDeletionShouldReturnSuccess() throws Exception {
        mockMvc().perform(delete("/api/v1/users/me/deletion-requests").principal(auth()))
                .andExpect(status().isNoContent());
    }

    @Test
    void requestDataExportShouldReturnCreated() throws Exception {
        Mockito.when(userSelfServiceInputPort.requestDataExport("user-1"))
                .thenReturn(new DataExportRequestView(
                        "exp-1",
                        "REQUESTED",
                        LocalDateTime.of(2026, 3, 20, 10, 0)));

        mockMvc().perform(post("/api/v1/users/me/data-exports").principal(auth()))
                .andExpect(status().isCreated())
                .andExpect(content().string(Matchers.containsString("exp-1")));
    }
}
