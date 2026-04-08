package com.ecommerce.identity.adapter.rest.dto.registration;

public record VerifyOtpResponse(
        String regId,
        String verificationToken
) {}


