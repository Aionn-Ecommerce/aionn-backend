package com.aionn.ordering.infrastructure.persistence.repository;

import com.aionn.ordering.infrastructure.persistence.entity.OrderReturnEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderReturnRepository extends JpaRepository<OrderReturnEntity, String> {

    List<OrderReturnEntity> findByStatusOrderByRequestedAtDesc(String status, Pageable pageable);

    List<OrderReturnEntity> findByUserIdOrderByRequestedAtDesc(String userId, Pageable pageable);

    List<OrderReturnEntity> findByMerchantIdOrderByRequestedAtDesc(String merchantId, Pageable pageable);
}
