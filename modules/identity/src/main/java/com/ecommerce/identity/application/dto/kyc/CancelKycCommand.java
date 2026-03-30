package com.ecommerce.identity.application.dto.kyc;

public record CancelKycCommand(
        String userId,
        String kycId
) {
}
