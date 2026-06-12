package com.aionn.shipping.adapter.rest.controller;

import com.aionn.shipping.application.dto.shipment.command.CarrierWebhookCommand;
import com.aionn.shipping.application.dto.shipment.result.ShipmentResult;
import com.aionn.shipping.application.service.ShipmentService;
import com.aionn.shipping.domain.exception.ShippingErrorCode;
import com.aionn.shipping.domain.exception.ShippingException;
import com.aionn.shipping.infrastructure.carrier.config.GhnProperties;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shipping/webhooks")
@RequiredArgsConstructor
@Tag(name = "Shipping - Webhook", description = "Carrier webhook entry point")
public class ShippingWebhookController {

        private final ShipmentService shipmentService;
        private final GhnProperties ghnProperties;

        @PostMapping("/carrier")
        @Operation(summary = "Carrier webhook")
        public ResponseEntity<ApiResponse<ShipmentResult>> carrierWebhook(
                        @RequestParam(name = "secret", required = false) String secret,
                        @Valid @RequestBody CarrierWebhookPayload payload) {
                verifySecret(secret);
                ShipmentResult result = shipmentService.applyCarrierWebhook(new CarrierWebhookCommand(
                                payload.trackingCode(), payload.type(), payload.currentLocation(), payload.statusDesc(),
                                payload.shipperName(), payload.shipperPhone(), payload.signatureUrl(), payload.reason(),
                                payload.warehouseId()));
                return ResponseEntity.ok(ApiResponse.success(result, "Webhook applied"));
        }

        private void verifySecret(String provided) {
                String expected = ghnProperties.webhookSecret();
                if (expected == null || expected.isBlank()) {
                        // Webhook auth disabled: only allowed in dev. Production must configure
                        // GHN_WEBHOOK_SECRET; the deployment YAML can enforce this.
                        return;
                }
                if (provided == null || !expected.equals(provided)) {
                        throw new ShippingException(ShippingErrorCode.SHIPMENT_FORBIDDEN,
                                        "Invalid webhook secret");
                }
        }

        public record CarrierWebhookPayload(
                        @NotBlank String trackingCode,
                        @NotBlank String type,
                        String currentLocation,
                        String statusDesc,
                        String shipperName,
                        String shipperPhone,
                        String signatureUrl,
                        String reason,
                        String warehouseId) {
        }
}
