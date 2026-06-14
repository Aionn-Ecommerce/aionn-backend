package com.aionn.ucp.adapter.rest.controller;

import com.aionn.ucp.application.dto.checkout.CheckoutDtos;
import com.aionn.ucp.application.service.UcpCheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@RequestMapping(value = "/ucp/v1/checkout-sessions", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "UCP - Checkout", description = "UCP checkout capability (create / get / update / complete / cancel)")
public class UcpCheckoutController {

    private final UcpCheckoutService checkoutService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create checkout session (UCP)")
    public ResponseEntity<CheckoutDtos.CheckoutResponse> create(
            Authentication authentication,
            @Valid @RequestBody CheckoutDtos.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(checkoutService.create(request, authentication.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get checkout session (UCP)")
    public ResponseEntity<CheckoutDtos.CheckoutResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(checkoutService.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update checkout session (UCP)",
            description = "Full replacement of the checkout resource per UCP spec.")
    public ResponseEntity<CheckoutDtos.CheckoutResponse> update(
            Authentication authentication,
            @PathVariable String id,
            @Valid @RequestBody CheckoutDtos.UpdateRequest request) {
        return ResponseEntity.ok(checkoutService.update(id, authentication.getName(), request));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Complete checkout session (UCP)")
    public ResponseEntity<CheckoutDtos.CheckoutResponse> complete(
            Authentication authentication,
            @PathVariable String id,
            @RequestBody(required = false) CheckoutDtos.CompleteRequest request) {
        return ResponseEntity.ok(checkoutService.complete(id, authentication.getName(), request));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel checkout session (UCP)")
    public ResponseEntity<CheckoutDtos.CheckoutResponse> cancel(
            Authentication authentication,
            @PathVariable String id) {
        return ResponseEntity.ok(checkoutService.cancel(id, authentication.getName()));
    }
}
