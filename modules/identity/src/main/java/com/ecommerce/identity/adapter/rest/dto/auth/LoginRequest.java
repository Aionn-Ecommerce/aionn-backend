package com.ecommerce.identity.adapter.rest.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Identity is required") String identity,
        @NotBlank(message = "Password is required") String password) {
}
