package com.aionn.ordering.infrastructure.persistence.repository;

import com.aionn.ordering.infrastructure.persistence.entity.OrderReturnEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderReturnRepository extends JpaRepository<OrderReturnEntity, String> {
}

