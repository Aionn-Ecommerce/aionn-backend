package com.aionn.promotion.adapter.rest.controller;

import com.aionn.promotion.adapter.rest.dto.voucher.ApplyVoucherRequest;
import com.aionn.promotion.adapter.rest.dto.voucher.ReleaseVoucherRequest;
import com.aionn.promotion.adapter.rest.dto.voucher.ReserveVoucherRequest;
import com.aionn.promotion.application.dto.voucher.command.VoucherCommands;
import com.aionn.promotion.application.dto.voucher.result.UserVoucherResult;
import com.aionn.promotion.application.service.VoucherService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions/vouchers")
@RequiredArgsConstructor
@Tag(name = "Promotion - Voucher", description = "User voucher claim/reserve/apply/release")
public class VoucherController {

        private final VoucherService voucherService;

        @PostMapping("/{voucherCode}/claim")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Claim voucher")
        public ResponseEntity<ApiResponse<UserVoucherResult>> claim(
                        Authentication authentication,
                        @PathVariable String voucherCode) {
                return ApiResponse.createdResponse("Voucher claimed",
                                voucherService.claim(new VoucherCommands.ClaimVoucher(authentication.getName(),
                                                voucherCode)));
        }

        @PostMapping("/{voucherCode}/reserve")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Reserve voucher for an order")
        public ResponseEntity<ApiResponse<UserVoucherResult>> reserve(
                        Authentication authentication,
                        @PathVariable String voucherCode,
                        @Valid @RequestBody ReserveVoucherRequest request) {
                return ResponseEntity.ok(ApiResponse.success(
                                voucherService.reserve(new VoucherCommands.ReserveVoucher(
                                                authentication.getName(), voucherCode, request.orderId(),
                                                request.orderValue(),
                                                request.currency(), request.orderCategoryIds(), request.expiresAt())),
                                "Voucher reserved"));
        }

        @PostMapping("/{voucherCode}/apply")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Apply voucher to a paid order")
        public ResponseEntity<ApiResponse<UserVoucherResult>> apply(
                        Authentication authentication,
                        @PathVariable String voucherCode,
                        @Valid @RequestBody ApplyVoucherRequest request) {
                return ResponseEntity.ok(ApiResponse.success(
                                voucherService.apply(new VoucherCommands.ApplyVoucher(
                                                authentication.getName(), voucherCode, request.orderId(),
                                                request.appliedAmount(), request.currency())),
                                "Voucher applied"));
        }

        @PostMapping("/{voucherCode}/release")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Release voucher reservation")
        public ResponseEntity<ApiResponse<UserVoucherResult>> release(
                        Authentication authentication,
                        @PathVariable String voucherCode,
                        @Valid @RequestBody ReleaseVoucherRequest request) {
                return ResponseEntity.ok(ApiResponse.success(
                                voucherService.release(new VoucherCommands.ReleaseVoucher(
                                                authentication.getName(), voucherCode, request.orderId(),
                                                request.reason())),
                                "Voucher released"));
        }

        @GetMapping("/me")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "List my vouchers")
        public ResponseEntity<ApiResponse<List<UserVoucherResult>>> listMine(
                        Authentication authentication,
                        @RequestParam(defaultValue = "50") int limit) {
                int safeLimit = Math.min(Math.max(limit, 1), 100);
                return ResponseEntity.ok(ApiResponse.success(
                                voucherService.listMine(authentication.getName(), safeLimit), "Vouchers fetched"));
        }

        @GetMapping("/me/{voucherCode}")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get my voucher")
        public ResponseEntity<ApiResponse<UserVoucherResult>> getMine(
                        Authentication authentication,
                        @PathVariable String voucherCode) {
                return ResponseEntity.ok(ApiResponse.success(
                                voucherService.getMine(authentication.getName(), voucherCode), "Voucher fetched"));
        }
}
