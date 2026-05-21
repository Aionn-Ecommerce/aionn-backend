package com.aionn.inventory.infrastructure.persistence.repository;

import com.aionn.inventory.infrastructure.persistence.entity.StockAdjustmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockAdjustmentJpaRepository extends JpaRepository<StockAdjustmentEntity, String> {
}

