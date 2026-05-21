package com.aionn.catalog.adapter.rest.controller;

import com.aionn.catalog.adapter.rest.dto.merchant.AdminReasonRequest;
import com.aionn.catalog.adapter.rest.dto.merchant.RegisterMerchantRequest;
import com.aionn.catalog.adapter.rest.dto.merchant.UpdateMerchantProfileRequest;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog/merchants")
@RequiredArgsConstructor
@Tag(name = "Catalog - Merchant", description = "Catalog module: merchant storefront lifecycle endpoints")
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Register merchant", description = "UC3.1 - bootstrap a merchant storefront for the authenticated seller")
    public ResponseEntity<ApiResponse<MerchantResult>> register(
            Authentication authentication,
            @Valid @RequestBody RegisterMerchantRequest request) {
        MerchantResult result = merchantService.register(
                new RegisterMerchantCommand(authentication.getName(), request.name()));
        return ApiResponse.createdResponse("Merchant registered", result);
    }

    @PutMapping("/{merchantId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update merchant profile", description = "UC3.2 - update merchant display profile")
    public ResponseEntity<ApiResponse<MerchantResult>> updateProfile(
            Authentication authentication,
            @PathVariable String merchantId,
            @Valid @RequestBody UpdateMerchantProfileRequest request) {
        MerchantResult result = merchantService.updateProfile(new UpdateMerchantProfileCommand(
                merchantId, authentication.getName(), request.name(), request.logoUrl(), request.description()));
        return ResponseEntity.ok(ApiResponse.success(result, "Merchant profile updated"));
    }

    @PostMapping("/{merchantId}/suspend")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
    @Operation(summary = "Suspend merchant", description = "UC3.3 - CS Admin temporarily disables a storefront")
    public ResponseEntity<ApiResponse<MerchantResult>> suspend(
            Authentication authentication,
            @PathVariable String merchantId,
            @Valid @RequestBody AdminReasonRequest request) {
        MerchantResult result = merchantService.suspend(
                new SuspendMerchantCommand(merchantId, authentication.getName(), request.reason()));
        return ResponseEntity.ok(ApiResponse.success(result, "Merchant suspended"));
    }

    @PostMapping("/{merchantId}/activate")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
    @Operation(summary = "Activate merchant", description = "UC3.4 - CS Admin restores a previously suspended storefront")
    public ResponseEntity<ApiResponse<MerchantResult>> activate(
            Authentication authentication,
            @PathVariable String merchantId,
            @Valid @RequestBody AdminReasonRequest request) {
        MerchantResult result = merchantService.activate(
                new ActivateMerchantCommand(merchantId, authentication.getName(), request.reason()));
        return ResponseEntity.ok(ApiResponse.success(result, "Merchant activated"));
    }

    @PostMapping("/{merchantId}/close")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Close merchant", description = "UC3.5 - permanently close the storefront once orders are settled")
    public ResponseEntity<ApiResponse<MerchantResult>> close(
            Authentication authentication,
            @PathVariable String merchantId,
            @Valid @RequestBody AdminReasonRequest request) {
        MerchantResult result = merchantService.close(
                new CloseMerchantCommand(merchantId, authentication.getName(), request.reason()));
        return ResponseEntity.ok(ApiResponse.success(result, "Merchant closed"));
    }

    @GetMapping("/{merchantId}")
    @Operation(summary = "Get merchant", description = "Public read of merchant storefront")
    public ResponseEntity<ApiResponse<MerchantResult>> get(@PathVariable String merchantId) {
        return ResponseEntity.ok(ApiResponse.success(merchantService.get(merchantId), "Merchant fetched"));
    }
}

