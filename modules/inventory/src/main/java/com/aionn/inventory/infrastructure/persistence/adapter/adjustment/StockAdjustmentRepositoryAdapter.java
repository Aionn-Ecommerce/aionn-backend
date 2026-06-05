package com.aionn.inventory.infrastructure.persistence.adapter.adjustment;

import com.aionn.inventory.application.port.out.StockAdjustmentRepository;
import com.aionn.inventory.domain.model.StockAdjustment;
import com.aionn.inventory.infrastructure.persistence.mapper.StockAdjustmentDomainMapper;
import com.aionn.inventory.infrastructure.persistence.repository.StockAdjustmentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StockAdjustmentRepositoryAdapter implements StockAdjustmentRepository {

    private final StockAdjustmentJpaRepository jpa;
    private final StockAdjustmentDomainMapper mapper;

    @Override
    public StockAdjustment save(StockAdjustment adjustment) {
        return mapper.toDomain(jpa.save(mapper.toEntity(adjustment)));
    }
}

