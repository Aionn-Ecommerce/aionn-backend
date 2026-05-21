package com.aionn.payment.adapter.rest.controller;

import com.aionn.payment.adapter.rest.dto.method.LinkMethodRequest;
import com.aionn.payment.application.dto.method.command.PaymentMethodCommands;
import com.aionn.payment.application.dto.method.result.PaymentMethodResult;
import com.aionn.payment.application.service.PaymentMethodService;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments/methods")
@RequiredArgsConstructor
@Tag(name = "Payment - Method", description = "Stored payment method endpoints")
public class PaymentMethodController {

    private final PaymentMethodService methodService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Link method", description = "UC6.6")
    public ResponseEntity<ApiResponse<PaymentMethodResult>> link(
            Authentication authentication,
            @Valid @RequestBody LinkMethodRequest request) {
        PaymentMethodResult result = methodService.link(new PaymentMethodCommands.LinkMethod(
                authentication.getName(), request.provider(), request.last4Digits(), request.gatewayToken()));
        return ApiResponse.createdResponse("Payment method linked", result);
    }

    @PostMapping("/{methodId}/verify")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Verify method", description = "UC6.8")
    public ResponseEntity<ApiResponse<PaymentMethodResult>> verify(
            Authentication authentication,
            @PathVariable String methodId) {
        PaymentMethodResult result = methodService.verify(new PaymentMethodCommands.VerifyMethod(
                authentication.getName(), methodId));
        return ResponseEntity.ok(ApiResponse.success(result, "Payment method verified"));
    }

    @DeleteMapping("/{methodId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove method", description = "UC6.7")
    public ResponseEntity<Void> remove(
            Authentication authentication,
            @PathVariable String methodId) {
        methodService.remove(new PaymentMethodCommands.RemoveMethod(authentication.getName(), methodId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List my methods")
    public ResponseEntity<ApiResponse<List<PaymentMethodResult>>> listMine(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                methodService.listMine(authentication.getName()), "Methods fetched"));
    }
}

