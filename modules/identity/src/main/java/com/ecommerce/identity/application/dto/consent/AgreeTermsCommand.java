package com.ecommerce.identity.application.dto.consent;

public record AgreeTermsCommand(
        String userId,
        String version,
        String clientIp
) {
}
