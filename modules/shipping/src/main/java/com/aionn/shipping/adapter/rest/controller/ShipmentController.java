package com.aionn.shipping.adapter.rest.controller;

import com.aionn.shipping.adapter.rest.dto.shipment.CancelShipmentRequest;
import com.aionn.shipping.adapter.rest.dto.shipment.CreateShipmentRequest;
import com.aionn.shipping.adapter.rest.dto.shipment.QuoteShippingRequest;
import com.aionn.shipping.adapter.rest.dto.shipment.ResolveIssueRequest;
import com.aionn.shipping.adapter.rest.support.session.CurrentUserId;
import com.aionn.shipping.application.dto.rate.result.ShippingQuoteResult;
import com.aionn.shipping.application.dto.shipment.command.CancelShipmentCommand;
import com.aionn.shipping.application.dto.shipment.command.CreateShipmentCommand;
import com.aionn.shipping.application.dto.shipment.command.FetchLabelCommand;
import com.aionn.shipping.application.dto.shipment.command.QuoteShippingCommand;
import com.aionn.shipping.application.dto.shipment.command.ResolveIssueCommand;
import com.aionn.shipping.application.dto.shipment.result.ShipmentResult;
import com.aionn.shipping.application.service.ShipmentService;
import com.aionn.shipping.domain.exception.ShippingErrorCode;
import com.aionn.shipping.domain.exception.ShippingException;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import com.aionn.sharedkernel.integration.port.catalog.MerchantQueryPort;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/shipping/shipments")
@RequiredArgsConstructor
@Tag(name = "Shipping - Shipment", description = "Shipment lifecycle endpoints")
public class ShipmentController {

        private final ShipmentService shipmentService;
        private final MerchantQueryPort merchantQueryPort;

        @PostMapping("/quote")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Quote shipping fee")
        public ResponseEntity<ApiResponse<ShippingQuoteResult>> quote(
                        @Valid @RequestBody QuoteShippingRequest request) {
                ShippingQuoteResult result = shipmentService.quote(new QuoteShippingCommand(
                                request.orderId(), request.address(), request.dimensions(), request.currency()));
                return ResponseEntity.ok(ApiResponse.success(result, "Quote computed"));
        }

        @PostMapping
        @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
        @Operation(summary = "Create shipment")
        public ResponseEntity<ApiResponse<ShipmentResult>> create(
                        @CurrentUserId String ownerId,
                        @Valid @RequestBody CreateShipmentRequest request) {
                String merchantId = merchantQueryPort.findMerchantIdByOwnerId(ownerId)
                                .orElseThrow(() -> new ShippingException(ShippingErrorCode.SHIPMENT_FORBIDDEN));
                ShipmentResult result = shipmentService.createShipment(new CreateShipmentCommand(
                                request.orderId(), merchantId, request.userId(), request.address(),
                                request.dimensions(), request.codAmount(), request.shippingFee(),
                                request.currency()));
                return ApiResponse.createdResponse("Shipment created", result);
        }

        @PostMapping("/{shipmentId}/register")
        @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
        @Operation(summary = "Register shipment with carrier")
        public ResponseEntity<ApiResponse<ShipmentResult>> register(@PathVariable String shipmentId) {
                return ResponseEntity.ok(ApiResponse.success(
                                shipmentService.registerWithCarrier(shipmentId),
                                "Shipment registered with carrier"));
        }

        @PostMapping("/{shipmentId}/label")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Fetch shipping label")
        public ResponseEntity<ApiResponse<ShipmentResult>> fetchLabel(
                        @CurrentUserId String userId,
                        @PathVariable String shipmentId) {
                return ResponseEntity.ok(ApiResponse.success(
                                shipmentService.fetchLabel(new FetchLabelCommand(shipmentId, userId)),
                                "Label fetched"));
        }

        @PostMapping("/{shipmentId}/cancel")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Cancel shipment (merchant-only)")
        public ResponseEntity<ApiResponse<ShipmentResult>> cancel(
                        @CurrentUserId String userId,
                        @PathVariable String shipmentId,
                        @Valid @RequestBody CancelShipmentRequest request) {
                return ResponseEntity.ok(ApiResponse.success(
                                shipmentService.cancel(new CancelShipmentCommand(shipmentId, request.reason(),
                                                userId)),
                                "Shipment cancelled"));
        }

        @PostMapping("/{shipmentId}/issue")
        @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
        @Operation(summary = "Resolve issue")
        public ResponseEntity<ApiResponse<ShipmentResult>> resolveIssue(
                        @PathVariable String shipmentId,
                        @Valid @RequestBody ResolveIssueRequest request) {
                return ResponseEntity.ok(ApiResponse.success(
                                shipmentService.resolveIssue(new ResolveIssueCommand(
                                                shipmentId, request.issueType(), request.resolution())),
                                "Issue resolved"));
        }

        @GetMapping("/{shipmentId}")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get shipment for the authenticated viewer (buyer or seller)")
        public ResponseEntity<ApiResponse<ShipmentResult>> get(
                        @CurrentUserId String userId,
                        @PathVariable String shipmentId) {
                return ResponseEntity.ok(ApiResponse.success(
                                shipmentService.get(shipmentId, userId),
                                "Shipment fetched"));
        }

        @GetMapping("/by-order/{orderId}")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "List shipments for an order, filtered by viewer ownership")
        public ResponseEntity<ApiResponse<List<ShipmentResult>>> listByOrder(
                        @CurrentUserId String userId,
                        @PathVariable String orderId) {
                return ResponseEntity.ok(ApiResponse.success(
                                shipmentService.findByOrderId(orderId, userId),
                                "Shipments fetched"));
        }
}
