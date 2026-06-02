package com.aionn.identity.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_sessions", indexes = {
        @Index(name = "idx_session_user_id", columnList = "user_id"),
        @Index(name = "idx_session_status", columnList = "status"),
        @Index(name = "idx_session_expires_at", columnList = "expires_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AuthSessionEntity {

    @Id
    @Column(name = "session_id", nullable = false, length = 26)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Optimistic-lock counter. A user can revoke a session from device A while
     * device B
     * is concurrently extending {@code expiresAt} or bumping {@code lastActiveAt}
     * on a
     * refresh; without {@code @Version} the second writer would silently overwrite
     * the
     * revocation. JPA will instead throw
     * {@link jakarta.persistence.OptimisticLockException}
     * so the caller can retry or surface the conflict.
     */
    @Version
    @Column(name = "version", nullable = false)
    private long version;
}
