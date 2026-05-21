package com.aionn.ordering.infrastructure.adapter;

import com.aionn.ordering.application.port.out.OrderRepository;
import com.aionn.ordering.domain.model.Order;
import com.aionn.ordering.infrastructure.persistence.entity.OrderEntity;
import com.aionn.ordering.infrastructure.persistence.mapper.OrderDomainMapper;
import com.aionn.ordering.infrastructure.persistence.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpa;
    private final OrderDomainMapper mapper;

    @Override
    public Order save(Order order) {
        OrderEntity existing = jpa.findById(order.getOrderId()).orElse(null);
        OrderEntity entity = mapper.toEntity(order, existing);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return jpa.findById(orderId).map(mapper::toDomain);
    }

    @Override
    public List<Order> findByUser(String userId, int limit) {
        return jpa.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, Math.max(1, limit))).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Order> findPendingOlderThan(Instant cutoff, int limit) {
        return jpa.findPendingOlderThan(cutoff, PageRequest.of(0, Math.max(1, limit))).stream()
                .map(mapper::toDomain)
                .toList();
    }
}

