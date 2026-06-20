package com.aionn.catalog.adapter.rest.controller;

import com.aionn.catalog.adapter.rest.dto.merchant.AdminReasonRequest;
import com.aionn.catalog.adapter.rest.dto.merchant.RegisterMerchantRequest;
import com.aionn.catalog.adapter.rest.dto.merchant.UpdateMerchantProfileRequest;
import com.aionn.catalog.adapter.rest.support.session.CurrentAdminId;
import com.aionn.catalog.adapter.rest.support.session.CurrentOwnerId;
import com.aionn.catalog.application.dto.merchant.command.ActivateMerchantCommand;
import com.aionn.catalog.application.dto.merchant.command.CloseMerchantCommand;
import com.aionn.catalog.application.dto.merchant.command.RegisterMerchantCommand;
import com.aionn.catalog.application.dto.merchant.command.SuspendMerchantCommand;
import com.aionn.catalog.application.dto.merchant.command.UpdateMerchantProfileCommand;
import com.aionn.catalog.application.dto.merchant.result.MerchantResult;
import com.aionn.catalog.application.service.MerchantService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog/merchants")
@RequiredArgsConstructor
@Tag(name = "Catalog - Merchant", description = "Merchant storefront lifecycle endpoints")
public class MerchantController {

        private final MerchantService merchantService;

        @PostMapping
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Register merchant", description = "Bootstrap a merchant storefront for the authenticated seller")
        public ResponseEntity<ApiResponse<MerchantResult>> register(
                        @CurrentOwnerId String ownerId,
                        @Valid @RequestBody RegisterMerchantRequest request) {
                MerchantResult result = merchantService.register(
                                new RegisterMerchantCommand(ownerId, request.name()));
                return ApiResponse.createdResponse("Merchant registered", result);
        }

        @PutMapping("/{merchantId}")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Update merchant profile", description = "Update merchant display name, logo, description")
        public ResponseEntity<ApiResponse<MerchantResult>> updateProfile(
                        @CurrentOwnerId String ownerId,
                        @PathVariable String merchantId,
                        @Valid @RequestBody UpdateMerchantProfileRequest request) {
                MerchantResult result = merchantService.updateProfile(new UpdateMerchantProfileCommand(
                                merchantId, ownerId, request.name(), request.logoUrl(),
                                request.description(), request.provinceCode()));
                return ResponseEntity.ok(ApiResponse.success(result, "Merchant profile updated"));
        }

        @PostMapping("/{merchantId}/suspend")
        @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
        @Operation(summary = "Suspend merchant", description = "Admin temporarily disables a storefront")
        public ResponseEntity<ApiResponse<MerchantResult>> suspend(
                        @CurrentAdminId String adminId,
                        @PathVariable String merchantId,
                        @Valid @RequestBody AdminReasonRequest request) {
                MerchantResult result = merchantService.suspend(
                                new SuspendMerchantCommand(merchantId, adminId, request.reason()));
                return ResponseEntity.ok(ApiResponse.success(result, "Merchant suspended"));
        }

        @PostMapping("/{merchantId}/activate")
        @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
        @Operation(summary = "Activate merchant", description = "Admin restores a previously suspended storefront")
        public ResponseEntity<ApiResponse<MerchantResult>> activate(
                        @CurrentAdminId String adminId,
                        @PathVariable String merchantId,
                        @Valid @RequestBody AdminReasonRequest request) {
                MerchantResult result = merchantService.activate(
                                new ActivateMerchantCommand(merchantId, adminId, request.reason()));
                return ResponseEntity.ok(ApiResponse.success(result, "Merchant activated"));
        }

        @PostMapping("/{merchantId}/close")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Close merchant", description = "Permanently close the storefront once open orders are settled")
        public ResponseEntity<ApiResponse<MerchantResult>> close(
                        @CurrentOwnerId String ownerId,
                        @PathVariable String merchantId,
                        @Valid @RequestBody AdminReasonRequest request) {
                MerchantResult result = merchantService.close(
                                new CloseMerchantCommand(merchantId, ownerId, request.reason()));
                return ResponseEntity.ok(ApiResponse.success(result, "Merchant closed"));
        }

        @GetMapping("/me")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get my merchant", description = "Resolve the merchant storefront owned by the authenticated user")
        public ResponseEntity<ApiResponse<MerchantResult>> getMine(@CurrentOwnerId String ownerId) {
                return ResponseEntity.ok(ApiResponse.success(
                                merchantService.getByOwner(ownerId), "Merchant fetched"));
        }

        @GetMapping("/{merchantId}")
        @Operation(summary = "Get merchant", description = "Public read of merchant storefront")
        public ResponseEntity<ApiResponse<MerchantResult>> get(@PathVariable String merchantId) {
                return ResponseEntity.ok(ApiResponse.success(merchantService.get(merchantId), "Merchant fetched"));
        }

        @GetMapping
        @Operation(summary = "List merchants", description = "Public list of all merchants on the platform")
        public ResponseEntity<ApiResponse<List<MerchantResult>>> list(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {
                List<MerchantResult> results = merchantService.list(page, size);
                return ResponseEntity.ok(ApiResponse.success(results, "Merchants listed"));
        }
}
