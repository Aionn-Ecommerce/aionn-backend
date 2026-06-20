package com.aionn.payment.adapter.rest.controller;

import com.aionn.payment.adapter.rest.dto.method.CompleteStripeSetupIntentRequest;
import com.aionn.payment.adapter.rest.dto.method.LinkMethodRequest;
import com.aionn.payment.adapter.rest.dto.preference.UpdatePaymentPreferenceRequest;
import com.aionn.payment.adapter.rest.support.session.CurrentUserId;
import com.aionn.payment.application.dto.method.command.LinkMethodCommand;
import com.aionn.payment.application.dto.method.command.RemoveMethodCommand;
import com.aionn.payment.application.dto.method.command.VerifyMethodCommand;
import com.aionn.payment.application.dto.method.result.PaymentMethodResult;
import com.aionn.payment.application.dto.method.result.StripeSetupIntentResult;
import com.aionn.payment.application.dto.preference.result.PaymentPreferenceResult;
import com.aionn.payment.application.service.PaymentMethodService;
import com.aionn.payment.application.service.PaymentPreferenceService;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments/methods")
@RequiredArgsConstructor
@Tag(name = "Payment - Method", description = "Stored payment method endpoints")
public class PaymentMethodController {

    private final PaymentMethodService methodService;
    private final PaymentPreferenceService preferenceService;

    @GetMapping("/preference")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get preferred payment option")
    public ResponseEntity<ApiResponse<PaymentPreferenceResult>> preference(@CurrentUserId String userId) {
        return ResponseEntity.ok(ApiResponse.success(
                preferenceService.get(userId), "Payment preference fetched"));
    }

    @PutMapping("/preference")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update preferred payment option")
    public ResponseEntity<ApiResponse<PaymentPreferenceResult>> updatePreference(
            @CurrentUserId String userId,
            @Valid @RequestBody UpdatePaymentPreferenceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                preferenceService.update(userId, request.paymentType(), request.paymentMethodId()),
                "Payment preference updated"));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Link method")
    public ResponseEntity<ApiResponse<PaymentMethodResult>> link(
            @CurrentUserId String userId,
            @Valid @RequestBody LinkMethodRequest request) {
        PaymentMethodResult result = methodService.link(new LinkMethodCommand(
                userId, request.provider(), request.last4Digits(), request.gatewayToken()));
        return ApiResponse.createdResponse("Payment method linked", result);
    }

    @PostMapping("/{methodId}/verify")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Verify method")
    public ResponseEntity<ApiResponse<PaymentMethodResult>> verify(
            @CurrentUserId String userId,
            @PathVariable String methodId) {
        PaymentMethodResult result = methodService.verify(new VerifyMethodCommand(
                userId, methodId));
        return ResponseEntity.ok(ApiResponse.success(result, "Payment method verified"));
    }

    @PostMapping("/stripe/setup-intents")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create Stripe card setup intent")
    public ResponseEntity<ApiResponse<StripeSetupIntentResult>> createStripeSetupIntent(@CurrentUserId String userId) {
        StripeSetupIntentResult result = methodService.createStripeSetupIntent(userId);
        return ResponseEntity.ok(ApiResponse.success(result, "Stripe setup intent created"));
    }

    @PostMapping("/stripe/setup-intents/complete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Complete Stripe card setup intent")
    public ResponseEntity<ApiResponse<PaymentMethodResult>> completeStripeSetupIntent(
            @CurrentUserId String userId,
            @Valid @RequestBody CompleteStripeSetupIntentRequest request) {
        PaymentMethodResult result = methodService.completeStripeSetupIntent(
                userId, request.setupIntentId());
        return ApiResponse.createdResponse("Payment method linked", result);
    }

    @DeleteMapping("/{methodId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove method")
    public ResponseEntity<Void> remove(
            @CurrentUserId String userId,
            @PathVariable String methodId) {
        methodService.remove(new RemoveMethodCommand(userId, methodId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List my methods")
    public ResponseEntity<ApiResponse<List<PaymentMethodResult>>> listMine(@CurrentUserId String userId) {
        return ResponseEntity.ok(ApiResponse.success(
                methodService.listMine(userId), "Methods fetched"));
    }
}
