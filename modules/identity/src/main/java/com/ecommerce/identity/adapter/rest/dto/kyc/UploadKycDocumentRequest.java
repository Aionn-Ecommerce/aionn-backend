package com.ecommerce.identity.adapter.rest.dto.kyc;

import jakarta.validation.constraints.NotBlank;

public record UploadKycDocumentRequest(
        @NotBlank(message = "Blob URL is required")
        String blobUrl) {
}
