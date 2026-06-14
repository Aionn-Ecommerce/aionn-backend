package com.aionn.ucp.infrastructure.persistence.repository;

import com.aionn.ucp.infrastructure.persistence.entity.CartSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartSessionRepository extends JpaRepository<CartSessionEntity, String> {
    Optional<CartSessionEntity> findByUserId(String userId);
}
