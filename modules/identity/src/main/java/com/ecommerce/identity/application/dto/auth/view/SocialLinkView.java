package com.ecommerce.identity.application.dto.auth.view;

import java.time.LocalDateTime;

public record SocialLinkView(
                String provider,
                String providerUserId,
                LocalDateTime linkedAt) {
}


