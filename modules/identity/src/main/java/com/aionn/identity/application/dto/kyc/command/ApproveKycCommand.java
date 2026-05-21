package com.aionn.identity.application.dto.kyc.command;

public record ApproveKycCommand(
                String adminId,
                String kycId) {
}

