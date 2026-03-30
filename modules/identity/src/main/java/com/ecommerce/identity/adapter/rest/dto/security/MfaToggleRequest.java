package com.ecommerce.identity.adapter.rest.dto.security;

import jakarta.validation.constraints.NotBlank;

public record MfaToggleRequest(
        @NotBlank(message = "Password is required")
        String password) {
}
