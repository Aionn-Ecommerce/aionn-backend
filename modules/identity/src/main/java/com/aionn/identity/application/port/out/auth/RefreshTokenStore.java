package com.aionn.identity.application.port.out.auth;

import java.time.Duration;
import java.util.Optional;

/**
 * Port for opaque refresh-token storage. Tokens are random 256-bit values
 * mapped to a session id, with TTL applied at the storage layer.
 *
 * <p>
 * Hashing is up to the adapter implementation - typical choice is SHA-256
 * so a leaked store cannot replay tokens.
 */
public interface RefreshTokenStore {

    /** Persist a refresh token bound to a session, valid for {@code ttl}. */
    void store(String tokenId, String sessionId, Duration ttl);

    /** Lookup the session id behind a token (returns empty if expired/unknown). */
    Optional<String> findSessionId(String tokenId);

    /** Invalidate a refresh token (single-use rotation). */
    void revoke(String tokenId);

    /** Invalidate all refresh tokens belonging to a session. */
    void revokeBySessionId(String sessionId);
}

