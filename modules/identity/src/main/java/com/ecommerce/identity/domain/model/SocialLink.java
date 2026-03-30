package com.ecommerce.identity.domain.model;

import com.ecommerce.identity.domain.valueobject.AuthProvider;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SocialLink {

    private final String socialAccountId;
    private final String userId;
    private final AuthProvider provider;
    private final String providerUserId;
    private final LocalDateTime createdAt;

    public SocialLink(
            String socialAccountId,
            String userId,
            AuthProvider provider,
            String providerUserId,
            LocalDateTime createdAt) {
        this.socialAccountId = socialAccountId;
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.createdAt = createdAt;
    }

    public static SocialLink createNew(
            String socialAccountId,
            String userId,
            AuthProvider provider,
            String providerUserId) {
        return new SocialLink(socialAccountId, userId, provider, providerUserId, LocalDateTime.now());
    }

}
