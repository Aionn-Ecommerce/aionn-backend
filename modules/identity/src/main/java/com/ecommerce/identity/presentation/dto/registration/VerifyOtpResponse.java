package com.ecommerce.identity.presentation.dto.registration;

public record VerifyOtpResponse(
        String regId,
        String verificationToken
) {}
