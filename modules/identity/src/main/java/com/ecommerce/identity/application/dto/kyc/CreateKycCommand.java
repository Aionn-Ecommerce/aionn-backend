package com.ecommerce.identity.application.dto.kyc;

public record CreateKycCommand(
        String userId,
        String docType
) {
}
