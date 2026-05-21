package com.aionn.shipping.adapter.rest.controller;

import com.aionn.shipping.application.dto.shipment.command.ShipmentCommands;
import com.aionn.shipping.application.dto.shipment.result.ShipmentResult;
import com.aionn.shipping.application.service.ShipmentService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shipping/webhooks")
@RequiredArgsConstructor
@Tag(name = "Shipping - Webhook", description = "Carrier webhook entry point")
public class ShippingWebhookController {

    private final ShipmentService shipmentService;

    @PostMapping("/carrier")
    @Operation(summary = "Carrier webhook")
    public ResponseEntity<ApiResponse<ShipmentResult>> carrierWebhook(
            @Valid @RequestBody CarrierWebhookPayload payload) {
        ShipmentResult result = shipmentService.applyCarrierWebhook(new ShipmentCommands.CarrierWebhook(
                payload.trackingCode(), payload.type(), payload.currentLocation(), payload.statusDesc(),
                payload.shipperName(), payload.shipperPhone(), payload.signatureUrl(), payload.reason(),
                payload.warehouseId()));
        return ResponseEntity.ok(ApiResponse.success(result, "Webhook applied"));
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

