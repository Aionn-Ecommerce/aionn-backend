package com.ecommerce.identity.presentation.dto.registration;

import java.time.LocalDateTime;

public record CompleteRegistrationResponse(
        String userId,
        String displayName,
        LocalDateTime createdAt
) {
}