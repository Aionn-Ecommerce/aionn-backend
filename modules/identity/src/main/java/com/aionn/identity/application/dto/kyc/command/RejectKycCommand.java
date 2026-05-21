package com.aionn.identity.application.dto.kyc.command;

public record RejectKycCommand(
                String adminId,
                String kycId,
                String reason) {
}



