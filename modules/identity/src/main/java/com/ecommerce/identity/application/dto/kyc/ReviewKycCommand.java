package com.ecommerce.identity.application.dto.kyc;

public record ReviewKycCommand(
        String adminId,
        String kycId,
        String note
) {
}
