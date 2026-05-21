package com.aionn.identity.infrastructure.auth;

import com.aionn.identity.application.port.out.auth.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Redis-backed refresh token store. We hash the token id before persisting so
 * a leaked DB does not enable token replay.
 */
@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private static final String TOKEN_KEY_PREFIX = "identity:auth:refresh:";
    private static final String SESSION_INDEX_PREFIX = "identity:auth:refresh:session:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void store(String tokenId, String sessionId, Duration ttl) {
        String tokenKey = tokenKey(tokenId);
        redisTemplate.opsForValue().set(tokenKey, sessionId, ttl);
        redisTemplate.opsForSet().add(sessionIndexKey(sessionId), tokenId);
        redisTemplate.expire(sessionIndexKey(sessionId), ttl);
    }

    @Override
    public Optional<String> findSessionId(String tokenId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(tokenKey(tokenId)));
    }

    @Override
    public void revoke(String tokenId) {
        redisTemplate.delete(tokenKey(tokenId));
    }

    @Override
    public void revokeBySessionId(String sessionId) {
        String indexKey = sessionIndexKey(sessionId);
        var members = redisTemplate.opsForSet().members(indexKey);
        if (members != null) {
            for (String tokenId : members) {
                redisTemplate.delete(tokenKey(tokenId));
            }
        }
        redisTemplate.delete(indexKey);
    }

    private static String tokenKey(String tokenId) {
        return TOKEN_KEY_PREFIX + sha256(tokenId);
    }

    private static String sessionIndexKey(String sessionId) {
        return SESSION_INDEX_PREFIX + sessionId;
    }

    private static String sha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is required by every JVM platform.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}

