package com.ecommerce.identity.application.dto.registration;

import java.time.LocalDateTime;

public record InitiateRegistrationResult(
                String regId,
                LocalDateTime resendAvailableAt,
                LocalDateTime expiredAt,
                String otpCode) {
}
