package com.aionn.identity.infrastructure.persistence.repository.auth;

import com.aionn.identity.infrastructure.persistence.entity.AuthSessionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthSessionRepository extends JpaRepository<AuthSessionEntity, String> {

    List<AuthSessionEntity> findByUser_UserIdOrderByCreatedAtDesc(String userId);

    List<AuthSessionEntity> findByUser_UserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    default List<AuthSessionEntity> findRecentByUserId(String userId, Pageable pageable) {
        return findByUser_UserIdOrderByCreatedAtDesc(userId, pageable);
    }
}
