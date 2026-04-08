package com.ecommerce.identity.adapter.rest.dto.security;

import jakarta.validation.constraints.NotBlank;

public record UnlockAccountRequest(
        @NotBlank(message = "User ID is required")
        String userId) {
}


