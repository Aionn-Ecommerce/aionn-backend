package com.ecommerce.identity.application.dto.kyc;

public record UploadKycDocumentCommand(
        String userId,
        String kycId,
        String blobUrl
) {
}
