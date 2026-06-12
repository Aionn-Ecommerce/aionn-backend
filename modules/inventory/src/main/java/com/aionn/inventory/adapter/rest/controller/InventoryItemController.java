package com.aionn.inventory.adapter.rest.controller;

import com.aionn.inventory.adapter.rest.dto.inventory.AuditInventoryRequest;
import com.aionn.inventory.adapter.rest.dto.inventory.ConfigureSafetyStockRequest;
import com.aionn.inventory.adapter.rest.dto.inventory.EmergencyLockRequest;
import com.aionn.inventory.adapter.rest.dto.inventory.InitializeStockRequest;
import com.aionn.inventory.adapter.rest.dto.inventory.ManualAdjustmentRequest;
import com.aionn.inventory.adapter.rest.dto.inventory.TrackBatchAndExpiryRequest;
import com.aionn.inventory.application.dto.inventory.command.InventoryCommands;
import com.aionn.inventory.application.dto.inventory.result.InventoryItemResult;
import com.aionn.inventory.application.service.InventoryItemService;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory/items")
@RequiredArgsConstructor
@Tag(name = "Inventory - Item", description = "InventoryItem operations: initialize, safety stock, audit, lock")
public class InventoryItemController {

        private final InventoryItemService service;

        @PostMapping
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Initialize stock", description = "UC4.6")
        public ResponseEntity<ApiResponse<InventoryItemResult>> initialize(
                        Authentication authentication,
                        @Valid @RequestBody InitializeStockRequest request) {
                InventoryItemResult result = service.initialize(new InventoryCommands.InitializeStock(
                                authentication.getName(), request.skuId(), request.warehouseId(),
                                request.initialQty()));
                return ApiResponse.createdResponse("Inventory initialized", result);
        }

        @PutMapping("/{skuId}/{warehouseId}/safety-stock")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Configure safety stock", description = "UC4.19")
        public ResponseEntity<ApiResponse<InventoryItemResult>> configureSafetyStock(
                        Authentication authentication,
                        @PathVariable String skuId,
                        @PathVariable String warehouseId,
                        @Valid @RequestBody ConfigureSafetyStockRequest request) {
                InventoryItemResult result = service.configureSafetyStock(new InventoryCommands.ConfigureSafetyStock(
                                authentication.getName(), skuId, warehouseId, request.safetyStockQty()));
                return ResponseEntity.ok(ApiResponse.success(result, "Safety stock configured"));
        }

        @PutMapping("/{skuId}/{warehouseId}/batch-expiry")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Track batch and expiry", description = "UC4.11")
        public ResponseEntity<ApiResponse<InventoryItemResult>> trackBatchAndExpiry(
                        Authentication authentication,
                        @PathVariable String skuId,
                        @PathVariable String warehouseId,
                        @Valid @RequestBody TrackBatchAndExpiryRequest request) {
                InventoryItemResult result = service.trackBatchAndExpiry(new InventoryCommands.TrackBatchAndExpiry(
                                authentication.getName(), skuId, warehouseId, request.batchNo(), request.expiryDate()));
                return ResponseEntity.ok(ApiResponse.success(result, "Batch and expiry tracked"));
        }

        @PostMapping("/{skuId}/{warehouseId}/manual-adjustment")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Manual adjustment", description = "UC4.17")
        public ResponseEntity<ApiResponse<InventoryItemResult>> manualAdjustment(
                        Authentication authentication,
                        @PathVariable String skuId,
                        @PathVariable String warehouseId,
                        @Valid @RequestBody ManualAdjustmentRequest request) {
                InventoryItemResult result = service.manualAdjustment(new InventoryCommands.ManualAdjustment(
                                authentication.getName(), skuId, warehouseId, request.qty(), request.type(),
                                request.reason()));
                return ResponseEntity.ok(ApiResponse.success(result, "Adjustment recorded"));
        }

        @PostMapping("/{skuId}/{warehouseId}/lock")
        @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
        @Operation(summary = "Emergency lock", description = "UC4.5")
        public ResponseEntity<ApiResponse<InventoryItemResult>> emergencyLock(
                        Authentication authentication,
                        @PathVariable String skuId,
                        @PathVariable String warehouseId,
                        @Valid @RequestBody EmergencyLockRequest request) {
                InventoryItemResult result = service.emergencyLock(new InventoryCommands.EmergencyLock(
                                authentication.getName(), skuId, warehouseId, request.reason()));
                return ResponseEntity.ok(ApiResponse.success(result, "Inventory item locked"));
        }

        @PostMapping("/{skuId}/{warehouseId}/unlock")
        @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
        @Operation(summary = "Emergency unlock", description = "Lifts an emergency lock")
        public ResponseEntity<ApiResponse<InventoryItemResult>> emergencyUnlock(
                        Authentication authentication,
                        @PathVariable String skuId,
                        @PathVariable String warehouseId) {
                InventoryItemResult result = service.emergencyUnlock(new InventoryCommands.EmergencyUnlock(
                                authentication.getName(), skuId, warehouseId));
                return ResponseEntity.ok(ApiResponse.success(result, "Inventory item unlocked"));
        }

        @PostMapping("/{skuId}/{warehouseId}/audit")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Record audit", description = "UC4.12 - reconcile system count with physical count")
        public ResponseEntity<ApiResponse<InventoryItemResult>> auditInventory(
                        Authentication authentication,
                        @PathVariable String skuId,
                        @PathVariable String warehouseId,
                        @Valid @RequestBody AuditInventoryRequest request) {
                InventoryItemResult result = service.auditInventory(new InventoryCommands.AuditInventory(
                                authentication.getName(), skuId, warehouseId, request.actualQty()));
                return ResponseEntity.ok(ApiResponse.success(result, "Audit recorded"));
        }

        @GetMapping("/{skuId}/{warehouseId}")
        @Operation(summary = "Get inventory item")
        public ResponseEntity<ApiResponse<InventoryItemResult>> get(
                        @PathVariable String skuId,
                        @PathVariable String warehouseId) {
                return ResponseEntity
                                .ok(ApiResponse.success(service.get(skuId, warehouseId), "Inventory item fetched"));
        }
}
