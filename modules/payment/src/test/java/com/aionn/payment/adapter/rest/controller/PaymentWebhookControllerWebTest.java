package com.aionn.payment.adapter.rest.controller;

import com.aionn.payment.adapter.rest.exception.PaymentExceptionHandler;
import com.aionn.payment.application.dto.payment.command.ConfirmPaymentCommand;
import com.aionn.payment.application.dto.payment.command.FailPaymentCommand;
import com.aionn.payment.application.port.out.PaymentProviderClient;
import com.aionn.payment.application.port.out.PaymentProviderRouter;
import com.aionn.payment.application.service.PaymentService;
import com.aionn.payment.domain.valueobject.PaymentGatewayKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookControllerWebTest {

    @Mock
    private PaymentProviderRouter providerRouter;
    @Mock
    private PaymentService paymentService;
    @Mock
    private PaymentProviderClient providerClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PaymentWebhookController controller = new PaymentWebhookController(providerRouter, paymentService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new PaymentExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(
                        Jackson2ObjectMapperBuilder.json().build()))
                .build();
    }

    @Test
    void handleSuccessfulEventConfirmsPayment() throws Exception {
        PaymentProviderClient.WebhookEvent event = new PaymentProviderClient.WebhookEvent(
                "payment.captured", "pay-1", "txn-1",
                new BigDecimal("100"), "VND", true, null, null);
        when(providerRouter.route(PaymentGatewayKind.STRIPE)).thenReturn(providerClient);
        when(providerClient.verifyAndParse(any(), eq("sig"))).thenReturn(event);

        mockMvc.perform(post("/api/v1/payments/webhooks/stripe")
                        .header("X-Signature", "sig")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"raw\":true}"))
                .andExpect(status().isOk());

        verify(paymentService).confirm(any(ConfirmPaymentCommand.class));
        verify(paymentService, never()).fail(any());
    }

    @Test
    void handleFailedEventFailsPayment() throws Exception {
        PaymentProviderClient.WebhookEvent event = new PaymentProviderClient.WebhookEvent(
                "payment.failed", "pay-2", null,
                new BigDecimal("100"), "VND", false, "ERR", "boom");
        when(providerRouter.route(PaymentGatewayKind.VNPAY)).thenReturn(providerClient);
        when(providerClient.verifyAndParse(any(), any())).thenReturn(event);

        mockMvc.perform(post("/api/v1/payments/webhooks/vnpay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"raw\":true}"))
                .andExpect(status().isOk());

        verify(paymentService).fail(any(FailPaymentCommand.class));
        verify(paymentService, never()).confirm(any());
    }

    @Test
    void unknownGatewayReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/payments/webhooks/unknown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void webhookWithoutPaymentIdReturnsBadRequest() throws Exception {
        PaymentProviderClient.WebhookEvent event = new PaymentProviderClient.WebhookEvent(
                "ping", null, null, null, null, true, null, null);
        when(providerRouter.route(PaymentGatewayKind.STRIPE)).thenReturn(providerClient);
        when(providerClient.verifyAndParse(any(), any())).thenReturn(event);

        mockMvc.perform(post("/api/v1/payments/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).confirm(any());
        verify(paymentService, never()).fail(any());
    }
}
