package com.aionn.identity.adapter.rest.dto.kyc;

import jakarta.validation.constraints.NotBlank;

public record ReviewKycRequest(
        @NotBlank(message = "Admin note is required")
        String note) {
}



