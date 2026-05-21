package com.aionn.shipping.adapter.rest.controller;

import com.aionn.shipping.adapter.rest.dto.shipment.CancelShipmentRequest;
import com.aionn.shipping.adapter.rest.dto.shipment.CreateShipmentRequest;
import com.aionn.shipping.adapter.rest.dto.shipment.FetchLabelRequest;
import com.aionn.shipping.adapter.rest.dto.shipment.QuoteShippingRequest;
import com.aionn.shipping.adapter.rest.dto.shipment.ResolveIssueRequest;
import com.aionn.shipping.application.dto.rate.result.ShippingQuoteResult;
import com.aionn.shipping.application.dto.shipment.command.ShipmentCommands;
import com.aionn.shipping.application.dto.shipment.result.ShipmentResult;
import com.aionn.shipping.application.service.ShipmentService;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shipping/shipments")
@RequiredArgsConstructor
@Tag(name = "Shipping - Shipment", description = "Shipment lifecycle endpoints")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create shipment", description = "UC7.1")
    public ResponseEntity<ApiResponse<ShipmentResult>> create(@Valid @RequestBody CreateShipmentRequest request) {
        ShipmentResult result = shipmentService.createShipment(new ShipmentCommands.CreateShipment(
                request.orderId(), request.address(), request.dimensions(),
                request.codAmount(), request.shippingFee(), request.currency()));
        return ApiResponse.createdResponse("Shipment created", result);
    }

    @PostMapping("/quote")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Quote shipping fee", description = "UC7.2")
    public ResponseEntity<ApiResponse<ShippingQuoteResult>> quote(@Valid @RequestBody QuoteShippingRequest request) {
        ShippingQuoteResult result = shipmentService.quote(new ShipmentCommands.QuoteShipping(
                request.orderId(), request.address(), request.dimensions(), request.currency()));
        return ResponseEntity.ok(ApiResponse.success(result, "Quote computed"));
    }

    @PostMapping("/{shipmentId}/register")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Register with carrier", description = "UC7.3")
    public ResponseEntity<ApiResponse<ShipmentResult>> register(@PathVariable String shipmentId) {
        return ResponseEntity.ok(ApiResponse.success(shipmentService.registerWithCarrier(shipmentId),
                "Shipment registered with carrier"));
    }

    @PostMapping("/{shipmentId}/label")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Fetch shipping label", description = "UC7.4")
    public ResponseEntity<ApiResponse<ShipmentResult>> fetchLabel(
            @PathVariable String shipmentId,
            @Valid @RequestBody FetchLabelRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                shipmentService.fetchLabel(new ShipmentCommands.FetchLabel(shipmentId, request.merchantId())),
                "Label fetched"));
    }

    @PostMapping("/{shipmentId}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel shipment", description = "UC7.11")
    public ResponseEntity<ApiResponse<ShipmentResult>> cancel(
            @PathVariable String shipmentId,
            @Valid @RequestBody CancelShipmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                shipmentService.cancel(new ShipmentCommands.CancelShipment(shipmentId, request.reason())),
                "Shipment cancelled"));
    }

    @PostMapping("/{shipmentId}/issue")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
    @Operation(summary = "Resolve issue", description = "UC7.13")
    public ResponseEntity<ApiResponse<ShipmentResult>> resolveIssue(
            @PathVariable String shipmentId,
            @Valid @RequestBody ResolveIssueRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                shipmentService.resolveIssue(new ShipmentCommands.ResolveIssue(
                        shipmentId, request.issueType(), request.resolution())),
                "Issue resolved"));
    }

    @GetMapping("/{shipmentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get shipment")
    public ResponseEntity<ApiResponse<ShipmentResult>> get(@PathVariable String shipmentId) {
        return ResponseEntity.ok(ApiResponse.success(shipmentService.get(shipmentId), "Shipment fetched"));
    }
}

