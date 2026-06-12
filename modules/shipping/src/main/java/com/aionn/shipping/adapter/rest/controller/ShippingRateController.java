package com.aionn.shipping.adapter.rest.controller;

import com.aionn.shipping.adapter.rest.dto.rate.ConfigureRateRequest;
import com.aionn.shipping.adapter.rest.dto.rate.UpdateRateRequest;
import com.aionn.shipping.application.dto.rate.command.ConfigureRateCommand;
import com.aionn.shipping.application.dto.rate.command.UpdateRateCommand;
import com.aionn.shipping.application.dto.rate.result.ShippingRateResult;
import com.aionn.shipping.application.service.ShippingRateService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shipping/rates")
@RequiredArgsConstructor
@Tag(name = "Shipping - Rate", description = "System Admin shipping rate configuration")
public class ShippingRateController {

    private final ShippingRateService rateService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Configure rate")
    public ResponseEntity<ApiResponse<ShippingRateResult>> configure(
            @Valid @RequestBody ConfigureRateRequest request) {
        ShippingRateResult result = rateService.configure(new ConfigureRateCommand(
                request.zoneCode(), request.baseFee(), request.currency(), request.condition()));
        return ApiResponse.createdResponse("Shipping rate configured", result);
    }

    @PutMapping("/{rateId}")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    @Operation(summary = "Update rate")
    public ResponseEntity<ApiResponse<ShippingRateResult>> update(
            @PathVariable String rateId,
            @Valid @RequestBody UpdateRateRequest request) {
        ShippingRateResult result = rateService.update(new UpdateRateCommand(
                rateId, request.baseFee(), request.condition()));
        return ResponseEntity.ok(ApiResponse.success(result, "Shipping rate updated"));
    }

    @GetMapping("/{rateId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get rate")
    public ResponseEntity<ApiResponse<ShippingRateResult>> get(@PathVariable String rateId) {
        return ResponseEntity.ok(ApiResponse.success(rateService.get(rateId), "Rate fetched"));
    }
}
