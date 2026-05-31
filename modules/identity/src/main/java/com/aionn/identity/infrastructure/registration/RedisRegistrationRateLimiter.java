package com.aionn.identity.infrastructure.registration;

import com.aionn.identity.application.port.out.registration.RegistrationRateLimiterPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "identity.registration.ratelimit", name = "provider", havingValue = "redis", matchIfMissing = true)
@RequiredArgsConstructor
public class RedisRegistrationRateLimiter implements RegistrationRateLimiterPort {

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
        redisTemplate.opsForZSet().removeRangeByScore(bucket, Double.NEGATIVE_INFINITY, windowStart);
        Long count = redisTemplate.opsForZSet().size(bucket);
        if (count != null && count >= maxAttempts) {
            log.debug("Rate limit exceeded: scope={}, key={}, count={}, max={}",
                    scope, key, count, maxAttempts);
            return false;
        }
        String member = now + ":" + Thread.currentThread().threadId();
        redisTemplate.opsForZSet().add(bucket, member, now);
        redisTemplate.expire(bucket, Duration.ofSeconds(windowSeconds * 2L));

        return true;
    }
}
