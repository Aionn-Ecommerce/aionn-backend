package com.aionn.ordering.infrastructure.persistence.repository;

import com.aionn.ordering.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, String> {

    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("""
            SELECT o FROM OrderEntity o
              WHERE o.status = 'PENDING'
                AND o.createdAt <= :cutoff
            ORDER BY o.createdAt ASC
            """)
    List<OrderEntity> findPendingOlderThan(Instant cutoff, Pageable pageable);
}

