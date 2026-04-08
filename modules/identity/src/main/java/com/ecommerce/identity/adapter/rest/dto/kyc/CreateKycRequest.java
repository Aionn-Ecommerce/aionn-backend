package com.ecommerce.identity.adapter.rest.dto.kyc;

import jakarta.validation.constraints.NotBlank;

public record CreateKycRequest(
        @NotBlank(message = "Document type is required")
        String docType) {
}


