package com.aionn.identity.infrastructure.registration;

import com.aionn.identity.application.port.out.registration.RegistrationRateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Redis-backed sliding-window rate limiter for registration.
 * Uses a sorted set per bucket; scores are epoch seconds.
 * <p>
 * Multi-pod safe. Keys auto-expire after 2× the window to prevent unbounded
 * growth.
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class RedisRegistrationRateLimiter implements RegistrationRateLimiter {

    private static final String KEY_PREFIX = "identity:ratelimit:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean check(String scope, String key, int maxAttempts, int windowSeconds) {
        if (key == null || key.isBlank()) {
            return true;
        }

        String bucket = KEY_PREFIX + scope + ":" + key;
        long now = Instant.now().getEpochSecond();
        long windowStart = now - windowSeconds;

        // Remove entries outside the window
        redisTemplate.opsForZSet().removeRangeByScore(bucket, Double.NEGATIVE_INFINITY, windowStart);

        // Count remaining entries
        Long count = redisTemplate.opsForZSet().zCard(bucket);
        if (count != null && count >= maxAttempts) {
            log.debug("Rate limit exceeded: scope={}, key={}, count={}, max={}",
                    scope, key, count, maxAttempts);
            return false;
        }

        // Add current request
        String member = now + ":" + Thread.currentThread().getId();
        redisTemplate.opsForZSet().add(bucket, member, now);

        // Set TTL to 2× window to auto-cleanup
        redisTemplate.expire(bucket, Duration.ofSeconds(windowSeconds * 2L));

        return true;
    }
}
