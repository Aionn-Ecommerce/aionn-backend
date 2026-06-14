package com.aionn.ucp.adapter.rest.controller;

import com.aionn.ucp.application.dto.cart.CartDtos;
import com.aionn.ucp.application.service.UcpCartService;
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
@RequestMapping(value = "/ucp/v1/carts", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "UCP - Cart", description = "UCP cart capability - lightweight basket building before checkout")
public class UcpCartController {

    private final UcpCartService cartService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create cart session (UCP)", description = "Create a lightweight cart session for pre-purchase exploration")
    public ResponseEntity<CartDtos.CartResponse> create(
            Authentication authentication,
            @Valid @RequestBody CartDtos.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.create(request, authentication.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get cart session (UCP)")
    public ResponseEntity<CartDtos.CartResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(cartService.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update cart session (UCP)", description = "Full replacement of the cart resource per UCP spec")
    public ResponseEntity<CartDtos.CartResponse> update(
            Authentication authentication,
            @PathVariable String id,
            @Valid @RequestBody CartDtos.UpdateRequest request) {
        return ResponseEntity.ok(cartService.update(id, authentication.getName(), request));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel cart session (UCP)")
    public ResponseEntity<CartDtos.CartResponse> cancel(
            Authentication authentication,
            @PathVariable String id) {
        return ResponseEntity.ok(cartService.cancel(id, authentication.getName()));
    }
}
