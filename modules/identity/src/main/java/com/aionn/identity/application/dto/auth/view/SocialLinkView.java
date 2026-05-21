package com.aionn.identity.application.dto.auth.view;

import java.time.LocalDateTime;

/**
 * @deprecated Prefer
 *             {@link com.aionn.identity.application.dto.auth.result.SocialLinkResult}.
 */
@Deprecated(since = "2.0", forRemoval = true)
public record SocialLinkView(
        String provider,
        String providerUserId,
        LocalDateTime linkedAt) {
}

