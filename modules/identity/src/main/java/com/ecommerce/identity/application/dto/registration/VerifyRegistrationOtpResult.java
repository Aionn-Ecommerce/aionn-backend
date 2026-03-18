package com.ecommerce.identity.application.dto.registration;

public record VerifyRegistrationOtpResult(
        String regId,
        String verificationToken) {
}
