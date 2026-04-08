package com.ecommerce.identity.infrastructure.security;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryPasswordResetTokenStore {

    public record PasswordResetTokenData(String userId, LocalDateTime expiresAt) {}

    private final Map<String, PasswordResetTokenData> tokens = new ConcurrentHashMap<>();

    public void save(String token, String userId, LocalDateTime expiresAt) {
        tokens.put(token, new PasswordResetTokenData(userId, expiresAt));
    }

    public Optional<PasswordResetTokenData> find(String token) {
        return Optional.ofNullable(tokens.get(token));
    }

    public void delete(String token) {
        tokens.remove(token);
    }
}


