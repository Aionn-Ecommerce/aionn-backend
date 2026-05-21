package com.aionn.identity.application.port.out.auth;

/**
 * Token blacklist for emergency revocation (logout, ban, password change).
 * <p>
 * In Option B (trust JWT expiry), access tokens are short-lived (15 min)
 * and don't require per-request session DB lookups. However, when a user
 * logs out or is banned, we blacklist the token's JTI (JWT ID) so it's
 * rejected immediately rather than waiting for natural expiry.
 * <p>
 * Entries auto-expire from the blacklist when the token itself would have
 * expired (no unbounded growth).
 */
public interface TokenBlacklist {

    /**
     * Add a token to the blacklist.
     *
     * @param jti        the JWT ID (unique per token)
     * @param ttlSeconds how long to keep it blacklisted (= remaining token
     *                   lifetime)
     */
    void blacklist(String jti, long ttlSeconds);

    /**
     * Check if a token is blacklisted.
     */
    boolean isBlacklisted(String jti);
}
