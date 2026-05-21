package com.aionn.identity.adapter.rest.dto.kyc;

import jakarta.validation.constraints.NotBlank;

public record RejectKycRequest(
        @NotBlank(message = "Reason is required")
        String reason) {
}



