package com.ecommerce.identity.adapter.rest.dto.consent;

import jakarta.validation.constraints.NotBlank;

public record TermsConsentRequest(
        @NotBlank(message = "Version is required")
        String version) {
}


