package com.ecommerce.identity.domain.model;

import com.ecommerce.identity.domain.valueobject.AuthSessionStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AuthSession {

    private final String sessionId;
    private final String userId;
    private final String ipAddress;
    private final String userAgent;
    private AuthSessionStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;
    private final LocalDateTime expiresAt;

    public AuthSession(
            String sessionId,
            String userId,
            String ipAddress,
            String userAgent,
            AuthSessionStatus status,
            LocalDateTime createdAt,
            LocalDateTime lastActiveAt,
            LocalDateTime expiresAt) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.status = status;
        this.createdAt = createdAt;
        this.lastActiveAt = lastActiveAt;
        this.expiresAt = expiresAt;
    }

    public static AuthSession createNew(
            String sessionId,
            String userId,
            String ipAddress,
            String userAgent,
            LocalDateTime expiresAt) {
        LocalDateTime now = LocalDateTime.now();
        return new AuthSession(
                sessionId,
                userId,
                ipAddress,
                userAgent,
                AuthSessionStatus.ACTIVE,
                now,
                now,
                expiresAt);
    }

    public void touch() {
        this.lastActiveAt = LocalDateTime.now();
    }

    public void revoke() {
        this.status = AuthSessionStatus.REVOKED;
    }

}
