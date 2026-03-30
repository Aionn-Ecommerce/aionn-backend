package com.ecommerce.identity.application.dto.consent;

public record AgreePrivacyCommand(
        String userId,
        String version,
        String clientIp
) {
}
