package com.ecommerce.identity.adapter.rest.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LinkSocialRequest(
        @NotBlank(message = "Provider is required")
        @Pattern(regexp = "^(?i)(google|facebook)$", message = "Provider must be google or facebook")
        String provider,
        @NotBlank(message = "Provider token is required")
        String providerToken) {
}


