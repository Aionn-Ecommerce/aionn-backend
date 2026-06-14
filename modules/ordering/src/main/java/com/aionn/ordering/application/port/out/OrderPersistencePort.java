package com.aionn.ordering.application.port.out;

import com.aionn.ordering.domain.model.Order;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderPersistencePort {

    Order save(Order order);

    Optional<Order> findById(String orderId);

    List<Order> findByUser(String userId, int limit);

    /** Sweep: order ids still PENDING past the deadline (used by auto-cancel). */
    List<String> findPendingOrderIdsOlderThan(Instant cutoff, int limit);
}
