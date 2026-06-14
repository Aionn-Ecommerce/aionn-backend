package com.aionn.ordering.infrastructure.persistence.repository;

import com.aionn.ordering.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {

  @Override
  @EntityGraph(attributePaths = "items")
  Optional<OrderEntity> findById(String orderId);

  @EntityGraph(attributePaths = "items")
  List<OrderEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

  /**
   * Auto-cancel sweep returns ids only; the worker re-loads each one in its own
   * tx.
   */
  @Query("""
      SELECT o.orderId FROM OrderEntity o
        WHERE o.status = 'PENDING'
          AND o.createdAt <= :cutoff
      ORDER BY o.createdAt ASC
      """)
  List<String> findPendingOrderIdsOlderThan(Instant cutoff, Pageable pageable);

  boolean existsByMerchantIdAndStatusNotIn(String merchantId, Collection<String> terminalStatuses);
}
