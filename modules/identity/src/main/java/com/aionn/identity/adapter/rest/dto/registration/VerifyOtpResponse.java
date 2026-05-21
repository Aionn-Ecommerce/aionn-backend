package com.aionn.identity.adapter.rest.dto.registration;

public record VerifyOtpResponse(
        String regId,
        String verificationToken
) {}



