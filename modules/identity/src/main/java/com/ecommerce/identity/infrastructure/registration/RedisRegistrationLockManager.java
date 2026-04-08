package com.ecommerce.identity.infrastructure.registration;

import com.ecommerce.identity.application.port.out.registration.RegistrationLockManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisRegistrationLockManager implements RegistrationLockManager {

    private static final String LOCK_PREFIX = "identity:registration:lock:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean tryLock(String phoneNumber, int timeoutSeconds) {
        String key = LOCK_PREFIX + phoneNumber;
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, "LOCKED", Duration.ofSeconds(timeoutSeconds));
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unlock(String phoneNumber) {
        String key = LOCK_PREFIX + phoneNumber;
        redisTemplate.delete(key);
    }
}
