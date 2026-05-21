package com.aionn.identity.application.dto.auth.view;

import java.time.LocalDateTime;

/**
 * @deprecated Prefer
 *             {@link com.aionn.identity.application.dto.auth.result.AuthSessionResult}.
 *             Kept for compile-time backward compatibility while controllers
 *             migrate.
 */
@Deprecated(since = "2.0", forRemoval = true)
public record AuthSessionView(
        String sessionId,
        String userId,
        String status,
        String ipAddress,
        String userAgent,
        LocalDateTime createdAt,
        LocalDateTime lastActiveAt,
        LocalDateTime expiresAt) {
}

