package com.ecommerce.identity.domain.model;

import com.ecommerce.identity.domain.valueobject.AuthProvider;

import java.time.LocalDateTime;

public record SocialLink(
        String socialAccountId,
        String userId,
        AuthProvider provider,
        String providerUserId,
        LocalDateTime createdAt) {

    public static SocialLink createNew(
            String socialAccountId,
            String userId,
            AuthProvider provider,
            String providerUserId) {
        return new SocialLink(socialAccountId, userId, provider, providerUserId, LocalDateTime.now());
    }
}
