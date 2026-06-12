package com.aionn.ordering.infrastructure.persistence.repository;

import com.aionn.ordering.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, String> {

  /** Eagerly fetch items so the domain mapper can run after the tx closes. */
  @Override
  @EntityGraph(attributePaths = "items")
  Optional<OrderEntity> findById(String orderId);

  @EntityGraph(attributePaths = "items")
  List<OrderEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

  /**
   * Returns just the order ids of pending orders past the cutoff.
   * The auto-cancel sweep re-loads each one inside a per-row tx via the
   * worker, so we deliberately do NOT pull items here.
   */
  @Query("""
      SELECT o.orderId FROM OrderEntity o
        WHERE o.status = 'PENDING'
          AND o.createdAt <= :cutoff
      ORDER BY o.createdAt ASC
      """)
  List<String> findPendingOrderIdsOlderThan(Instant cutoff, Pageable pageable);

  /**
   * True if the merchant has at least one order whose status is not in the
   * supplied terminal set. Backed by {@code idx_orders_merchant} so the
   * lookup is O(log N) even with millions of historic rows.
   */
  boolean existsByMerchantIdAndStatusNotIn(String merchantId, java.util.Collection<String> terminalStatuses);
}
