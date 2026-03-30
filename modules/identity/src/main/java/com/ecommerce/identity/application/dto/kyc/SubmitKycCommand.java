package com.ecommerce.identity.application.dto.kyc;

public record SubmitKycCommand(
        String userId,
        String kycId
) {
}
