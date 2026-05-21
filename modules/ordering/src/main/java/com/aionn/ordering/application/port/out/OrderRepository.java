package com.aionn.ordering.application.port.out;

import com.aionn.ordering.domain.model.Order;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(String orderId);

    List<Order> findByUser(String userId, int limit);

    /** UC5.10 sweep: orders still PENDING past the deadline. */
    List<Order> findPendingOlderThan(Instant cutoff, int limit);
}

