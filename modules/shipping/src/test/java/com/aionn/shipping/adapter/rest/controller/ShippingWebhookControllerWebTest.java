package com.aionn.shipping.adapter.rest.controller;

import com.aionn.shipping.adapter.rest.exception.ShippingExceptionHandler;
import com.aionn.shipping.application.dto.shipment.command.CarrierWebhookCommand;
import com.aionn.shipping.application.dto.shipment.result.ShipmentResult;
import com.aionn.shipping.application.service.ShipmentService;
import com.aionn.shipping.infrastructure.carrier.config.GhnProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ShippingWebhookControllerWebTest {

    @Mock
    private ShipmentService shipmentService;

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    private MockMvc buildMockMvc(String webhookSecret) {
        GhnProperties props = new GhnProperties(
                "https://carrier", "token", "shop", 1, "ward", 1, null, null,
                null, null, webhookSecret);
        ShippingWebhookController controller = new ShippingWebhookController(shipmentService, props);
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ShippingExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private String validPayload() throws Exception {
        return objectMapper.writeValueAsString(
                new ShippingWebhookController.CarrierWebhookPayload(
                        "TRACK_1", "PICKED_UP", null, null, null, null, null, null, "WH_1"));
    }

    @Test
    void carrierWebhookAcceptsValidSecret() throws Exception {
        MockMvc mockMvc = buildMockMvc("expected-secret");
        when(shipmentService.applyCarrierWebhook(any(CarrierWebhookCommand.class)))
                .thenReturn(sample("S_1", "PICKED_UP"));

        mockMvc.perform(post("/api/v1/shipping/webhooks/carrier")
                        .header("X-Webhook-Secret", "expected-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shipmentId").value("S_1"));

        verify(shipmentService).applyCarrierWebhook(any(CarrierWebhookCommand.class));
    }

    @Test
    void carrierWebhookRejectsInvalidSecret() throws Exception {
        MockMvc mockMvc = buildMockMvc("expected-secret");

        mockMvc.perform(post("/api/v1/shipping/webhooks/carrier")
                        .header("X-Webhook-Secret", "WRONG")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isForbidden());

        verify(shipmentService, never()).applyCarrierWebhook(any());
    }

    @Test
    void carrierWebhookRejectsMissingSecretWhenConfigured() throws Exception {
        MockMvc mockMvc = buildMockMvc("expected-secret");

        mockMvc.perform(post("/api/v1/shipping/webhooks/carrier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isForbidden());

        verify(shipmentService, never()).applyCarrierWebhook(any());
    }

    @Test
    void carrierWebhookSkipsAuthWhenSecretNotConfigured() throws Exception {
        MockMvc mockMvc = buildMockMvc(null);
        when(shipmentService.applyCarrierWebhook(any(CarrierWebhookCommand.class)))
                .thenReturn(sample("S_1", "PICKED_UP"));

        mockMvc.perform(post("/api/v1/shipping/webhooks/carrier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isOk());

        verify(shipmentService).applyCarrierWebhook(any(CarrierWebhookCommand.class));
    }

    @Test
    void carrierWebhookValidatesPayload() throws Exception {
        MockMvc mockMvc = buildMockMvc(null);

        String invalid = objectMapper.writeValueAsString(
                new ShippingWebhookController.CarrierWebhookPayload(
                        " ", " ", null, null, null, null, null, null, null));

        mockMvc.perform(post("/api/v1/shipping/webhooks/carrier")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().is4xxClientError());

        verify(shipmentService, never()).applyCarrierWebhook(any());
    }

    private ShipmentResult sample(String id, String status) {
        Instant now = Instant.now();
        return new ShipmentResult(id, "ORDER_1", "M_1", "U_1",
                "TRACK_1", "CARRIER_1", null, BigDecimal.ZERO, BigDecimal.valueOf(30000), "VND",
                status, null, null, null, 0, null, null, null, null, null, null, now, now);
    }
}
