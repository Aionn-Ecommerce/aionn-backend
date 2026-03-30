package com.ecommerce.identity.application.dto.auth;

public record LinkSocialCommand(
        String userId,
        String provider,
        String providerToken) {
}
