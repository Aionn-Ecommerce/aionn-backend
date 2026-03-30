package com.ecommerce.identity.adapter.rest.dto.security;

import jakarta.validation.constraints.NotBlank;

public record CompletePasswordResetRequest(
        @NotBlank(message = "Reset token is required")
        String token,
        @NotBlank(message = "New password is required")
        String newPassword) {
}
