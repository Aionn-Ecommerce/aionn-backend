package com.ecommerce.identity.application.dto.kyc;

public record ApproveKycCommand(
        String adminId,
        String kycId
) {
}
