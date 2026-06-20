package com.aionn.promotion.adapter.rest.controller;

import com.aionn.promotion.adapter.rest.dto.voucher.IssueVoucherRequest;
import com.aionn.promotion.adapter.rest.support.session.CurrentUserId;
import com.aionn.promotion.application.dto.voucher.result.VoucherResult;
import com.aionn.promotion.application.service.ShopVoucherService;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions/shop-vouchers")
@RequiredArgsConstructor
@Tag(name = "Promotion - Shop Voucher", description = "Merchant-issued vouchers scoped to one shop")
public class ShopVoucherController {

    private final ShopVoucherService shopVoucherService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    @Operation(summary = "Issue a voucher for the authenticated merchant's shop")
    public ResponseEntity<ApiResponse<VoucherResult>> issue(
            @CurrentUserId String ownerId,
            @Valid @RequestBody IssueVoucherRequest request) {
        return ApiResponse.createdResponse("Shop voucher issued",
                shopVoucherService.issue(ownerId, request));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    @Operation(summary = "List vouchers issued by the authenticated merchant")
    public ResponseEntity<ApiResponse<List<VoucherResult>>> listMine(
            @CurrentUserId String ownerId,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                shopVoucherService.listMine(ownerId, limit), "Shop vouchers fetched"));
    }

    @GetMapping("/merchant/{merchantId}")
    @Operation(summary = "List collectible vouchers for a public shop page")
    public ResponseEntity<ApiResponse<List<VoucherResult>>> listByMerchant(
            @PathVariable String merchantId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                shopVoucherService.listByMerchant(merchantId, limit), "Shop vouchers fetched"));
    }
}
