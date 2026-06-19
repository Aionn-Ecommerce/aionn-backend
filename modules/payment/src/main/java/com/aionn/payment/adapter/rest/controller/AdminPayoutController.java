package com.aionn.payment.adapter.rest.controller;

import com.aionn.payment.adapter.rest.dto.payout.PayoutCompleteBody;
import com.aionn.payment.adapter.rest.dto.payout.PayoutFailBody;
import com.aionn.payment.adapter.rest.dto.payout.PayoutResponse;
import com.aionn.payment.application.service.PayoutService;
import com.aionn.payment.domain.model.MerchantPayout;
import com.aionn.payment.domain.valueobject.PayoutStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/payouts")
@RequiredArgsConstructor
@Tag(name = "Payment - Admin Payouts", description = "Admin payout management")
public class AdminPayoutController {

    private final PayoutService payoutService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
    @Operation(summary = "List payouts by status")
    public ResponseEntity<ApiResponse<List<PayoutResponse>>> list(
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(defaultValue = "100") int limit) {
        List<PayoutResponse> result = payoutService.listByStatus(PayoutStatus.valueOf(status), limit).stream()
                .map(AdminPayoutController::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.success(result, "Payouts fetched"));
    }

    @PostMapping("/{payoutId}/complete")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Mark payout completed (after bank transfer)")
    public ResponseEntity<ApiResponse<PayoutResponse>> complete(
            @PathVariable String payoutId,
            @Valid @RequestBody PayoutCompleteBody body) {
        MerchantPayout p = payoutService.markCompleted(payoutId, body.externalRef());
        return ResponseEntity.ok(ApiResponse.success(toResponse(p), "Payout completed"));
    }

    @PostMapping("/{payoutId}/fail")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Mark payout failed and refund balance")
    public ResponseEntity<ApiResponse<PayoutResponse>> fail(
            @PathVariable String payoutId,
            @Valid @RequestBody PayoutFailBody body) {
        MerchantPayout p = payoutService.markFailed(payoutId, body.reason());
        return ResponseEntity.ok(ApiResponse.success(toResponse(p), "Payout marked failed"));
    }

    private static PayoutResponse toResponse(MerchantPayout p) {
        return new PayoutResponse(p.getPayoutId(), p.getMerchantId(),
                p.getAmount(), p.getCurrency(), p.getStatus().name(),
                p.getBankName(), p.getBankAccountNo(), p.getBankAccountName(),
                p.getExternalRef(), p.getNote(),
                p.getRequestedAt(), p.getCompletedAt(), p.getFailedAt(), p.getFailureReason());
    }
}
