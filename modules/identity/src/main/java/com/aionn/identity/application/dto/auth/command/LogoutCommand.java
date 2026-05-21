package com.aionn.identity.application.dto.auth.command;

/**
 * @param accessTokenJti the JTI of the current access token to blacklist
 *                       (optional, null-safe)
 */
public record LogoutCommand(
        String userId,
        String sessionId,
        String accessTokenJti) {
}
