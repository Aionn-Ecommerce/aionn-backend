package com.ecommerce.identity.adapter.rest.dto.security;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequestCommand(
        @NotBlank(message = "Identity is required")
        String identity) {
}
