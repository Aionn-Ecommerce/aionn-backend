package com.aionn.payment.adapter.rest.controller;

import com.aionn.payment.adapter.rest.dto.payment.InitiatePaymentRequest;
import com.aionn.payment.adapter.rest.dto.payment.RefundRequest;
import com.aionn.payment.application.dto.payment.command.PaymentCommands;
import com.aionn.payment.application.dto.payment.result.PaymentResult;
import com.aionn.payment.application.service.PaymentService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment - Payment", description = "Payment lifecycle endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Initiate payment", description = "UC6.1")
    public ResponseEntity<ApiResponse<PaymentResult>> initiate(
            Authentication authentication,
            @Valid @RequestBody InitiatePaymentRequest request) {
        PaymentResult result = paymentService.initiate(new PaymentCommands.InitiatePayment(
                request.orderId(), authentication.getName(), request.paymentMethodId(),
                request.amount(), request.currency(), request.gateway(), request.idempotencyKey()));
        return ApiResponse.createdResponse("Payment initiated", result);
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
    @Operation(summary = "Refund payment", description = "UC6.5")
    public ResponseEntity<ApiResponse<PaymentResult>> refund(
            @PathVariable String paymentId,
            @Valid @RequestBody RefundRequest request) {
        PaymentResult result = paymentService.refund(new PaymentCommands.RefundPayment(
                paymentId, request.amount(), request.currency(), request.reason()));
        return ResponseEntity.ok(ApiResponse.success(result, "Payment refunded"));
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get payment", description = "UC6.4")
    public ResponseEntity<ApiResponse<PaymentResult>> get(@PathVariable String paymentId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.get(paymentId), "Payment fetched"));
    }
}

