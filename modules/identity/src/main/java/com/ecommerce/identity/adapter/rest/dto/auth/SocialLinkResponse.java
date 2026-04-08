package com.ecommerce.identity.adapter.rest.dto.auth;

import java.time.LocalDateTime;

public record SocialLinkResponse(
        String provider,
        String providerUserId,
        LocalDateTime linkedAt) {
}


