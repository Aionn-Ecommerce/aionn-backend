package com.aionn.ucp.infrastructure.persistence.repository;

import com.aionn.ucp.infrastructure.persistence.entity.CheckoutSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CheckoutSessionRepository extends JpaRepository<CheckoutSessionEntity, String> {

    Optional<CheckoutSessionEntity> findFirstByOrderId(String orderId);

    Optional<CheckoutSessionEntity> findFirstByCartId(String cartId);
}
