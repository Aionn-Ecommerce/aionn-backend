package com.aionn.ucp.infrastructure.persistence.repository;

import com.aionn.ucp.infrastructure.persistence.entity.OrderEventOutboxEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderEventOutboxJpaRepository extends JpaRepository<OrderEventOutboxEntity, String> {

    @Query("""
            SELECT e FROM OrderEventOutboxEntity e
             WHERE e.status = 'PENDING' AND e.attempts < :maxAttempts
             ORDER BY e.createdAt ASC
            """)
    List<OrderEventOutboxEntity> findPending(int maxAttempts, Pageable pageable);
}
