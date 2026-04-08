package com.ecommerce.identity.infrastructure.persistence.repository.auth;

import com.ecommerce.identity.infrastructure.persistence.entity.AuthSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthSessionRepository extends JpaRepository<AuthSessionEntity, String> {

    List<AuthSessionEntity> findByUser_UserIdOrderByCreatedAtDesc(String userId);

    int countByUser_UserIdAndStatus(String userId, String status);
}


