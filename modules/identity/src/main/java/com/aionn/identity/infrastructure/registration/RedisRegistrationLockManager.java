package com.aionn.identity.infrastructure.registration;

import com.aionn.identity.application.port.out.registration.RegistrationLockManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

/**
 * Distributed lock backed by Redis. Each acquire stamps the key with a unique
 * token; release runs a Lua script that only deletes the key when the token
 * matches, so two workers racing on the same phone number cannot delete each
 * other's lock.
 */
@Component
@RequiredArgsConstructor
public class RedisRegistrationLockManager implements RegistrationLockManager {

    private static final String LOCK_PREFIX = "identity:registration:lock:";
    private static final DefaultRedisScript<Long> RELEASE_SCRIPT;

    static {
        RELEASE_SCRIPT = new DefaultRedisScript<>(
                "if redis.call('GET', KEYS[1]) == ARGV[1] then "
                        + "return redis.call('DEL', KEYS[1]) "
                        + "else return 0 end",
                Long.class);
    }

    private final StringRedisTemplate redisTemplate;

    @Override
    public String tryLock(String phoneNumber, int timeoutSeconds) {
        String key = LOCK_PREFIX + phoneNumber;
        String token = UUID.randomUUID().toString();
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, token, Duration.ofSeconds(timeoutSeconds));
        return Boolean.TRUE.equals(success) ? token : "";
    }

    @Override
    public void unlock(String phoneNumber, String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        String key = LOCK_PREFIX + phoneNumber;
        redisTemplate.execute(RELEASE_SCRIPT, Collections.singletonList(key), token);
    }
}

