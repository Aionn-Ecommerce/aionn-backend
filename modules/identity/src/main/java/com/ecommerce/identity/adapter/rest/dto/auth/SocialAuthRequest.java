package com.ecommerce.identity.adapter.rest.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record SocialAuthRequest(
        @NotBlank(message = "Provider is required")
        String provider,
        @NotBlank(message = "Provider token is required")
        String providerToken) {
}


