package com.ecommerce.identity.infrastructure.registration;

import com.ecommerce.identity.application.port.out.registration.model.RegistrationVerificationSession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class RegistrationSessionRedisManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RegistrationRedisKeyManager keyManager;

    public RegistrationSessionRedisManager(
            RedisTemplate<String, Object> redisTemplate,
            RegistrationRedisKeyManager keyManager) {
        this.redisTemplate = redisTemplate;
        this.keyManager = keyManager;
    }

    public void save(RegistrationVerificationSession session) {
        String key = keyManager.registrationSessionKey(session.getRegId());
        Duration ttl = calculateTtl(session.getExpiredAt());
        redisTemplate.opsForValue().set(key, session, ttl);
    }

    public Optional<RegistrationVerificationSession> findByRegId(String regId) {
        String key = keyManager.registrationSessionKey(regId);
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof RegistrationVerificationSession session) {
            return Optional.of(session);
        }
        return Optional.empty();
    }

    public void deleteByRegId(String regId) {
        redisTemplate.delete(keyManager.registrationSessionKey(regId));
    }

    private Duration calculateTtl(LocalDateTime expiredAt) {
        if (expiredAt == null) {
            return Duration.ofMinutes(5);
        }
        long seconds = Duration.between(LocalDateTime.now(), expiredAt).getSeconds();
        if (seconds <= 0) {
            return Duration.ofSeconds(1);
        }
        return Duration.ofSeconds(seconds);
    }
}