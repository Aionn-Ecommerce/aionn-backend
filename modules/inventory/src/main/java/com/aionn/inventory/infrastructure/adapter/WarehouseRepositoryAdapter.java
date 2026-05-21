package com.aionn.inventory.infrastructure.adapter;

import com.aionn.inventory.application.port.out.WarehouseRepository;
import com.aionn.inventory.domain.model.Warehouse;
import com.aionn.inventory.infrastructure.persistence.entity.WarehouseEntity;
import com.aionn.inventory.infrastructure.persistence.mapper.WarehouseDomainMapper;
import com.aionn.inventory.infrastructure.persistence.repository.WarehouseJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WarehouseRepositoryAdapter implements WarehouseRepository {

    private final WarehouseJpaRepository jpa;
    private final WarehouseDomainMapper mapper;

    @Override
    public Warehouse save(Warehouse warehouse) {
        WarehouseEntity existing = jpa.findById(warehouse.getWarehouseId()).orElse(null);
        WarehouseEntity entity = mapper.toEntity(warehouse, existing);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Warehouse> findById(String warehouseId) {
        return jpa.findById(warehouseId).map(mapper::toDomain);
    }

    @Override
    public List<Warehouse> findByMerchantOrderByPriority(String merchantId) {
        return jpa.findByMerchantIdOrderByPriorityLevelAsc(merchantId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}

