package com.ecommerce.identity.application.dto.kyc.command;

public record ApproveKycCommand(
                String adminId,
                String kycId) {
}
