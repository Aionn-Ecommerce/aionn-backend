package com.aionn.payment.adapter.rest.controller;

import com.aionn.payment.adapter.rest.exception.PaymentExceptionHandler;
import com.aionn.payment.adapter.rest.support.session.CurrentUserIdArgumentResolver;
import com.aionn.payment.application.dto.method.command.LinkMethodCommand;
import com.aionn.payment.application.dto.method.command.RemoveMethodCommand;
import com.aionn.payment.application.dto.method.command.VerifyMethodCommand;
import com.aionn.payment.application.dto.method.result.PaymentMethodResult;
import com.aionn.payment.application.dto.method.result.StripeSetupIntentResult;
import com.aionn.payment.application.dto.preference.result.PaymentPreferenceResult;
import com.aionn.payment.application.service.PaymentMethodService;
import com.aionn.payment.application.service.PaymentPreferenceService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentMethodControllerWebTest {

    @Mock
    private PaymentMethodService methodService;
    @Mock
    private PaymentPreferenceService preferenceService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PaymentMethodController controller = new PaymentMethodController(methodService, preferenceService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new PaymentExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(
                        Jackson2ObjectMapperBuilder.json().build()))
                .setCustomArgumentResolvers(new CurrentUserIdArgumentResolver())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "user-123", "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static PaymentMethodResult method(String methodId, String status) {
        Instant now = Instant.now();
        return new PaymentMethodResult(
                methodId, "user-123", "stripe", "4242", status,
                now, now, "VERIFIED".equals(status) ? now : null);
    }

    @Test
    void linkMethodReturnsCreated() throws Exception {
        when(methodService.link(any(LinkMethodCommand.class)))
                .thenReturn(method("m-1", "LINKED"));

        mockMvc.perform(post("/api/v1/payments/methods")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "stripe",
                                  "last4Digits": "4242",
                                  "gatewayToken": "tok-abc"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.methodId").value("m-1"))
                .andExpect(jsonPath("$.data.status").value("LINKED"));

        verify(methodService).link(any(LinkMethodCommand.class));
    }

    @Test
    void linkRejectsBlankToken() throws Exception {
        mockMvc.perform(post("/api/v1/payments/methods")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "stripe",
                                  "last4Digits": "4242",
                                  "gatewayToken": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    void verifyTransitionsToVerified() throws Exception {
        when(methodService.verify(any(VerifyMethodCommand.class)))
                .thenReturn(method("m-2", "VERIFIED"));

        mockMvc.perform(post("/api/v1/payments/methods/m-2/verify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.methodId").value("m-2"))
                .andExpect(jsonPath("$.data.status").value("VERIFIED"));

        verify(methodService).verify(any(VerifyMethodCommand.class));
    }

    @Test
    void removeReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/payments/methods/m-3"))
                .andExpect(status().isNoContent());

        verify(methodService).remove(any(RemoveMethodCommand.class));
    }

    @Test
    void listMineReturnsActiveMethods() throws Exception {
        when(methodService.listMine("user-123"))
                .thenReturn(List.of(method("m-1", "VERIFIED"), method("m-2", "LINKED")));

        mockMvc.perform(get("/api/v1/payments/methods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].methodId").value("m-1"))
                .andExpect(jsonPath("$.data[1].methodId").value("m-2"));

        verify(methodService).listMine("user-123");
    }

    @Test
    void getPreferenceReturnsPreference() throws Exception {
        when(preferenceService.get("user-123"))
                .thenReturn(new PaymentPreferenceResult("CARD", "m-1"));

        mockMvc.perform(get("/api/v1/payments/methods/preference"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentType").value("CARD"))
                .andExpect(jsonPath("$.data.paymentMethodId").value("m-1"));
    }

    @Test
    void updatePreferenceReturnsUpdated() throws Exception {
        when(preferenceService.update("user-123", "CARD", "m-2"))
                .thenReturn(new PaymentPreferenceResult("CARD", "m-2"));

        mockMvc.perform(put("/api/v1/payments/methods/preference")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentType": "CARD",
                                  "paymentMethodId": "m-2"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentMethodId").value("m-2"));
    }

    @Test
    void createStripeSetupIntentReturnsClientSecret() throws Exception {
        when(methodService.createStripeSetupIntent("user-123"))
                .thenReturn(new StripeSetupIntentResult("si-1", "secret-1"));

        mockMvc.perform(post("/api/v1/payments/methods/stripe/setup-intents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.setupIntentId").value("si-1"))
                .andExpect(jsonPath("$.data.clientSecret").value("secret-1"));
    }

    @Test
    void completeStripeSetupIntentLinksMethod() throws Exception {
        when(methodService.completeStripeSetupIntent("user-123", "si-1"))
                .thenReturn(method("m-9", "VERIFIED"));

        mockMvc.perform(post("/api/v1/payments/methods/stripe/setup-intents/complete")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "setupIntentId": "si-1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.methodId").value("m-9"));
    }
}
