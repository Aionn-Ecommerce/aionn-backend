package com.aionn.identity.application.dto.kyc.command;

public record CancelKycCommand(
                String userId,
                String kycId) {
}



