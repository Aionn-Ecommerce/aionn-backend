package com.aionn.payment.adapter.rest.controller;

import com.aionn.payment.adapter.rest.dto.payment.InitiatePaymentRequest;
import com.aionn.payment.adapter.rest.dto.payment.RefundRequest;
import com.aionn.payment.adapter.rest.support.session.CurrentUserId;
import com.aionn.payment.application.dto.payment.command.InitiatePaymentCommand;
import com.aionn.payment.application.dto.payment.command.RefundPaymentCommand;
import com.aionn.payment.application.dto.payment.result.PaymentResult;
import com.aionn.payment.application.service.PaymentService;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment - Payment", description = "Payment lifecycle endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Initiate payment")
    public ResponseEntity<ApiResponse<PaymentResult>> initiate(
            @CurrentUserId String userId,
            @Valid @RequestBody InitiatePaymentRequest request) {
        PaymentResult result = paymentService.initiate(new InitiatePaymentCommand(
                request.orderId(), userId, request.paymentMethodId(),
                request.amount(), request.currency(), request.gateway(), request.idempotencyKey()));
        return ApiResponse.createdResponse("Payment initiated", result);
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
    @Operation(summary = "Refund payment")
    public ResponseEntity<ApiResponse<PaymentResult>> refund(
            @PathVariable String paymentId,
            @Valid @RequestBody RefundRequest request) {
        PaymentResult result = paymentService.refund(new RefundPaymentCommand(
                paymentId, request.amount(), request.currency(), request.reason()));
        return ResponseEntity.ok(ApiResponse.success(result, "Payment refunded"));
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get payment")
    public ResponseEntity<ApiResponse<PaymentResult>> get(
            @CurrentUserId String userId,
            @PathVariable String paymentId) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getForUser(paymentId, userId), "Payment fetched"));
    }

    @GetMapping("/by-order/{orderId}")
    @PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
    @Operation(summary = "List payments by order")
    public ResponseEntity<ApiResponse<List<PaymentResult>>> listByOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.listByOrderId(orderId), "Payments fetched"));
    }
}
