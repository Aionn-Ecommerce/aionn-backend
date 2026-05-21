package com.aionn.shipping.infrastructure.persistence.repository;

import com.aionn.shipping.infrastructure.persistence.entity.ShipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentJpaRepository extends JpaRepository<ShipmentEntity, String> {

    Optional<ShipmentEntity> findByTrackingCode(String trackingCode);

    List<ShipmentEntity> findByOrderId(String orderId);
}

