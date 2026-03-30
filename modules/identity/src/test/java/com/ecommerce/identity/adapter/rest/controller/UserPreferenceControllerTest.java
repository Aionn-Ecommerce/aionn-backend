package com.ecommerce.identity.adapter.rest.controller;

import com.ecommerce.identity.application.dto.preference.UserPreferenceResult;
import com.ecommerce.identity.application.port.in.preference.GetUserPreferenceInputPort;
import com.ecommerce.identity.application.port.in.preference.UpdateAiPrivacyPreferenceInputPort;
import com.ecommerce.identity.application.port.in.preference.UpdateGeneralPreferenceInputPort;
import com.ecommerce.identity.application.port.in.preference.UpdateNotificationPreferenceInputPort;
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

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserPreferenceControllerTest {

    @Mock
    private GetUserPreferenceInputPort getUserPreferenceInputPort;

    @Mock
    private UpdateGeneralPreferenceInputPort updateGeneralPreferenceInputPort;

    @Mock
    private UpdateNotificationPreferenceInputPort updateNotificationPreferenceInputPort;

    @Mock
    private UpdateAiPrivacyPreferenceInputPort updateAiPrivacyPreferenceInputPort;

    @InjectMocks
    private UserPreferenceController userPreferenceController;

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(userPreferenceController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    private Authentication auth() {
        return new UsernamePasswordAuthenticationToken("user-1", "N/A");
    }

    private UserPreferenceResult preference() {
        return new UserPreferenceResult(
                "user-1",
                "vi",
                "VND",
                "Asia/Ho_Chi_Minh",
                "light",
                "{\"email\":true}",
                "{\"allowTraining\":false}",
                LocalDateTime.of(2026, 3, 20, 10, 0));
    }

    @Test
    void getPreferencesShouldReturnSuccess() throws Exception {
        Mockito.when(getUserPreferenceInputPort.execute("user-1")).thenReturn(preference());

        mockMvc().perform(get("/api/v1/preferences").principal(auth()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Preferences fetched")));
    }

    @Test
    void updateGeneralShouldReturnSuccess() throws Exception {
        Mockito.when(updateGeneralPreferenceInputPort.execute(any()))
                .thenReturn(preference());

        mockMvc().perform(put("/api/v1/preferences/general")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"language":"vi","currency":"VND","timezone":"Asia/Ho_Chi_Minh","theme":"light"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("General preferences updated")));
    }

    @Test
    void updateNotificationsShouldReturnSuccess() throws Exception {
        Mockito.when(updateNotificationPreferenceInputPort.execute(any()))
                .thenReturn(preference());

        mockMvc().perform(put("/api/v1/preferences/notifications")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"notificationSettingsJson":"{\\"email\\":false}"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Notification preferences updated")));
    }

    @Test
    void updateAiPrivacyShouldReturnSuccess() throws Exception {
        Mockito.when(updateAiPrivacyPreferenceInputPort.execute(any()))
                .thenReturn(preference());

        mockMvc().perform(put("/api/v1/preferences/ai-privacy")
                        .principal(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"aiPrivacySettingsJson":"{\\"allowTraining\\":false}"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("AI privacy preferences updated")));
    }
}
