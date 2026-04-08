package com.ecommerce.identity.application.dto.kyc.command;

public record UploadKycDocumentCommand(
                String userId,
                String kycId,
                String blobUrl) {
}
