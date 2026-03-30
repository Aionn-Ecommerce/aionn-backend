package com.ecommerce.identity.application.dto.kyc;

public record RejectKycCommand(
        String adminId,
        String kycId,
        String reason
) {
}
