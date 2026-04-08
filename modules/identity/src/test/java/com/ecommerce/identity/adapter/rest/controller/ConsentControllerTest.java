package com.ecommerce.identity.adapter.rest.controller;

import com.ecommerce.sharedkernel.infrastructure.web.ClientIpResolver;
import com.ecommerce.identity.application.dto.consent.result.ConsentResult;
import com.ecommerce.identity.application.service.ConsentService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ConsentControllerTest {

        @Mock
        private ConsentService consentService;

        @Mock
        private ClientIpResolver clientIpResolver;

        @InjectMocks
        private ConsentController consentController;

        private MockMvc mockMvc() {
                return MockMvcBuilders.standaloneSetup(consentController)
                                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                                .build();
        }

        private Authentication auth() {
                return new UsernamePasswordAuthenticationToken("user-1", "N/A");
        }

        private ConsentResult consent(String type, String version) {
                return new ConsentResult(
                                "consent-1",
                                "user-1",
                                type,
                                version,
                                true,
                                LocalDateTime.of(2026, 3, 20, 10, 0),
                                null,
                                "127.0.0.1");
        }

        @Test
        void agreeTermsShouldReturnSuccess() throws Exception {
                Mockito.when(clientIpResolver.resolve(Mockito.any())).thenReturn("127.0.0.1");
                Mockito.when(consentService.agreeTerms("user-1", "v1", "127.0.0.1"))
                                .thenReturn(consent("TERMS", "v1"));

                mockMvc().perform(post("/api/v1/consents/terms")
                                .principal(auth())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"version":"v1"}
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(content().string(Matchers.containsString("\"consentType\":\"TERMS\"")));
        }

        @Test
        void agreePrivacyShouldReturnSuccess() throws Exception {
                Mockito.when(clientIpResolver.resolve(Mockito.any())).thenReturn("127.0.0.1");
                Mockito.when(consentService.agreePrivacy("user-1", "v1", "127.0.0.1"))
                                .thenReturn(consent("PRIVACY", "v1"));

                mockMvc().perform(post("/api/v1/consents/privacy")
                                .principal(auth())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"version":"v1"}
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(content().string(Matchers.containsString("\"consentType\":\"PRIVACY\"")));
        }

        @Test
        void updateMarketingShouldReturnSuccess() throws Exception {
                Mockito.when(clientIpResolver.resolve(Mockito.any())).thenReturn("127.0.0.1");
                Mockito.when(consentService.updateMarketing("user-1", true, "127.0.0.1"))
                                .thenReturn(consent("MARKETING", "v1"));

                mockMvc().perform(patch("/api/v1/consents/marketing")
                                .principal(auth())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"subscribed":true}
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(content().string(Matchers.containsString("\"consentType\":\"MARKETING\"")));
        }
}
