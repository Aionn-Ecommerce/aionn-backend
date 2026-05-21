package com.aionn.identity.infrastructure.user;

import com.aionn.identity.application.port.out.user.OtpChannel;
import com.aionn.identity.application.port.out.user.UserOtpChallengeStore;
import com.aionn.identity.application.port.out.user.UserOtpPurpose;
import com.aionn.identity.application.port.out.user.model.UserOtpChallenge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Redis-backed OTP challenge store. Replaces the in-memory implementation
 * for production use (multi-pod safe, survives restarts).
 * <p>
 * Key format: {@code identity:otp:{userId}:{purpose}}
 * Value format: pipe-delimited fields for simplicity (no JSON dependency).
 * TTL: aligned with the challenge expiry time.
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class RedisUserOtpChallengeStore implements UserOtpChallengeStore {

    private static final String KEY_PREFIX = "identity:otp:";
    private static final String DELIMITER = "|";
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    // Extra buffer beyond the stated expiry to avoid race conditions
    private static final Duration EXPIRY_BUFFER = Duration.ofMinutes(2);

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(UserOtpChallenge challenge) {
        String key = key(challenge.userId(), challenge.purpose());
        String value = serialize(challenge);

        Duration ttl = Duration.between(LocalDateTime.now(), challenge.expiresAt()).plus(EXPIRY_BUFFER);
        if (ttl.isNegative() || ttl.isZero()) {
            ttl = Duration.ofMinutes(10);
        }

        redisTemplate.opsForValue().set(key, value, ttl);
        log.debug("Saved OTP challenge: userId={}, purpose={}, ttl={}s",
                challenge.userId(), challenge.purpose(), ttl.getSeconds());
    }

    @Override
    public Optional<UserOtpChallenge> find(String userId, UserOtpPurpose purpose) {
        String key = key(userId, purpose);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(deserialize(value));
    }

    @Override
    public void delete(String userId, UserOtpPurpose purpose) {
        String key = key(userId, purpose);
        redisTemplate.delete(key);
        log.debug("Deleted OTP challenge: userId={}, purpose={}", userId, purpose);
    }

    private String key(String userId, UserOtpPurpose purpose) {
        return KEY_PREFIX + userId + ":" + purpose.name();
    }

    private String serialize(UserOtpChallenge c) {
        return String.join(DELIMITER,
                c.userId(),
                c.purpose().name(),
                c.channel() != null ? c.channel().name() : "",
                c.target() != null ? c.target() : "",
                c.otpCode(),
                c.pendingValue() != null ? c.pendingValue() : "",
                c.expiresAt().format(DT_FORMAT),
                String.valueOf(c.attempts()));
    }

    private UserOtpChallenge deserialize(String value) {
        String[] parts = value.split("\\|", -1);
        return new UserOtpChallenge(
                parts[0],
                UserOtpPurpose.valueOf(parts[1]),
                parts[2].isEmpty() ? null : OtpChannel.valueOf(parts[2]),
                parts[3].isEmpty() ? null : parts[3],
                parts[4],
                parts[5].isEmpty() ? null : parts[5],
                LocalDateTime.parse(parts[6], DT_FORMAT),
                Integer.parseInt(parts[7]));
    }
}
