package com.aionn.payment.adapter.rest.controller;

import com.aionn.payment.adapter.rest.dto.payout.PayoutRequestBody;
import com.aionn.payment.adapter.rest.dto.payout.PayoutResponse;
import com.aionn.payment.adapter.rest.dto.payout.MerchantBalanceResponse;
import com.aionn.payment.adapter.rest.support.session.CurrentUserId;
import com.aionn.payment.application.service.PayoutService;
import com.aionn.payment.domain.model.MerchantBalance;
import com.aionn.payment.domain.model.MerchantPayout;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments/merchant")
@RequiredArgsConstructor
@Tag(name = "Payment - Merchant Payouts", description = "Merchant balance + payout request endpoints")
public class MerchantPayoutController {

    private final PayoutService payoutService;

    @GetMapping("/balance")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    @Operation(summary = "Get my balance")
    public ResponseEntity<ApiResponse<MerchantBalanceResponse>> getBalance(
            @CurrentUserId String ownerId,
            @RequestParam(defaultValue = "VND") String currency) {
        MerchantBalance b = payoutService.getBalanceForOwner(ownerId, currency);
        return ResponseEntity.ok(ApiResponse.success(toResponse(b), "Balance fetched"));
    }

    @GetMapping("/payouts")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    @Operation(summary = "List my payouts")
    public ResponseEntity<ApiResponse<List<PayoutResponse>>> list(
            @CurrentUserId String ownerId,
            @RequestParam(defaultValue = "50") int limit) {
        List<PayoutResponse> result = payoutService.listForOwner(ownerId, limit).stream()
                .map(MerchantPayoutController::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.success(result, "Payouts fetched"));
    }

    @PostMapping("/payouts")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    @Operation(summary = "Request a payout")
    public ResponseEntity<ApiResponse<PayoutResponse>> request(
            @CurrentUserId String ownerId,
            @Valid @RequestBody PayoutRequestBody body) {
        MerchantPayout p = payoutService.requestPayout(ownerId,
                body.amount(), body.currency() == null ? "VND" : body.currency(),
                body.bankName(), body.bankAccountNo(), body.bankAccountName(), body.note());
        return ApiResponse.createdResponse("Payout requested", toResponse(p));
    }

    private static MerchantBalanceResponse toResponse(MerchantBalance b) {
        return new MerchantBalanceResponse(b.getMerchantId(), b.getCurrency(),
                b.getPending(), b.getAvailable(), b.getUpdatedAt());
    }

    private static PayoutResponse toResponse(MerchantPayout p) {
        return new PayoutResponse(p.getPayoutId(), p.getMerchantId(),
                p.getAmount(), p.getCurrency(), p.getStatus().name(),
                p.getBankName(), p.getBankAccountNo(), p.getBankAccountName(),
                p.getExternalRef(), p.getNote(),
                p.getRequestedAt(), p.getCompletedAt(), p.getFailedAt(), p.getFailureReason());
    }
}
