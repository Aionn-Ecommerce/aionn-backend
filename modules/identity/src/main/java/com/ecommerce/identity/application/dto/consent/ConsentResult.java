package com.ecommerce.identity.application.dto.consent;

import java.time.LocalDateTime;

public record ConsentResult(
        String consentId,
        String consentType,
        String version,
        LocalDateTime agreedAt,
        LocalDateTime revokedAt,
        String ipAddress
) {
}
