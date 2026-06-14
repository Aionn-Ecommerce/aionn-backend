package com.aionn.ordering.infrastructure.persistence.repository;

import com.aionn.ordering.infrastructure.persistence.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<CartEntity, String> {
    Optional<CartEntity> findByUserId(String userId);
}

