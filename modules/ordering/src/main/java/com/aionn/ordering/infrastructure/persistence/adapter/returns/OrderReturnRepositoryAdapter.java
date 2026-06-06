package com.aionn.ordering.infrastructure.persistence.adapter.returns;

import com.aionn.ordering.application.port.out.OrderReturnRepository;
import com.aionn.ordering.domain.model.OrderReturn;
import com.aionn.ordering.infrastructure.persistence.entity.OrderReturnEntity;
import com.aionn.ordering.infrastructure.persistence.mapper.OrderReturnDomainMapper;
import com.aionn.ordering.infrastructure.persistence.repository.OrderReturnJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderReturnRepositoryAdapter implements OrderReturnRepository {

    private final OrderReturnJpaRepository jpa;
    private final OrderReturnDomainMapper mapper;

    @Override
    public OrderReturn save(OrderReturn r) {
        OrderReturnEntity existing = jpa.findById(r.getReturnId()).orElse(null);
        OrderReturnEntity entity = mapper.toEntity(r, existing);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    public Optional<OrderReturn> findById(String returnId) {
        return jpa.findById(returnId).map(mapper::toDomain);
    }
}

