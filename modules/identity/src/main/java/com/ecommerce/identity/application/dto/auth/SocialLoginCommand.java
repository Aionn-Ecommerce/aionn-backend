package com.ecommerce.identity.application.dto.auth;

public record SocialLoginCommand(
        String provider,
        String providerToken,
        String ipAddress,
        String userAgent) {
}
