package com.ecommerce.identity.application.dto.auth;

import java.time.LocalDateTime;

public record SocialLinkView(
        String provider,
        String providerUserId,
        LocalDateTime linkedAt) {
}
