package com.ecommerce.identity.presentation.dto.registration;

import java.time.LocalDateTime;

public record InitiateRegistrationResponse(
                String regId,
                LocalDateTime resendAvailableAt,
                LocalDateTime expiredAt,
                String otpCode) {
}
