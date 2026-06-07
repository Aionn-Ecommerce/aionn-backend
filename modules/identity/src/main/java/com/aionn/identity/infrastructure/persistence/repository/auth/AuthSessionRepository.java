package com.aionn.identity.infrastructure.persistence.repository.auth;

import com.aionn.identity.infrastructure.persistence.entity.AuthSessionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthSessionRepository extends JpaRepository<AuthSessionEntity, String> {

    /**
     * Returns all sessions for a user, newest first.
     *
     * <p>
     * Used by management endpoints that need the full history (e.g. revoke-all,
     * security audit). Long-lived accounts can accumulate many rows here; if the UI
     * only needs a recent slice, prefer
     * {@link #findRecentByUserId(String, Pageable)}
     * instead and pass a small {@link Pageable} so the query stays bounded.
     * </p>
     */
    List<AuthSessionEntity> findByUser_UserIdOrderByCreatedAtDesc(String userId);

    List<AuthSessionEntity> findByUser_UserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * Convenience alias for paginated reads when only the most recent N rows
     * matter.
     */
    default List<AuthSessionEntity> findRecentByUserId(String userId, Pageable pageable) {
        return findByUser_UserIdOrderByCreatedAtDesc(userId, pageable);
    }
}
