package com.aionn.identity.application.dto.kyc.command;

public record ReviewKycCommand(
                String adminId,
                String kycId,
                String note) {
}



