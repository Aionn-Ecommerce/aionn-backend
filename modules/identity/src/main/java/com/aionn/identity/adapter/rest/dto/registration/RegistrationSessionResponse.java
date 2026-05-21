package com.aionn.identity.adapter.rest.dto.registration;

import java.time.LocalDateTime;

public record RegistrationSessionResponse(
        String regId,
        LocalDateTime resendAvailableAt,
        LocalDateTime expiredAt,
        String otpCode) {
}


