package com.aionn.identity.application.port.out.auth;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenStorePort {

    void store(String tokenId, String sessionId, Duration ttl);

    Optional<String> findSessionId(String tokenId);

    /**
     * Atomically reads and revokes the refresh token, returning the bound session
     * id.
     * <p>
     * This MUST be used during refresh token rotation to prevent replay attacks: a
     * single
     * compromised token can otherwise be redeemed twice if a {@code findSessionId}
     * +
     * {@code revoke} pair races with a concurrent refresh call.
     */
    Optional<String> consumeSessionId(String tokenId);

    void revoke(String tokenId);

    void revokeBySessionId(String sessionId);
}
