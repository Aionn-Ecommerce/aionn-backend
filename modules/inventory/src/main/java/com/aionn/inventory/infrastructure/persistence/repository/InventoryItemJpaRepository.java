package com.aionn.inventory.infrastructure.persistence.repository;

import com.aionn.inventory.infrastructure.persistence.entity.InventoryItemEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryItemJpaRepository
        extends JpaRepository<InventoryItemEntity, InventoryItemEntity.InventoryItemId> {

    /**
     * Pessimistic lock used to serialize concurrent reserve/commit/release on
     * the same row. Defined as a separate query method so the default
     * {@link #findById} stays free of locking and works inside read-only
     * transactions.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItemEntity i WHERE i.id.skuId = :skuId AND i.id.warehouseId = :warehouseId")
    Optional<InventoryItemEntity> findForUpdate(@Param("skuId") String skuId, @Param("warehouseId") String warehouseId);

    List<InventoryItemEntity> findByIdSkuIdAndIdWarehouseIdIn(String skuId, List<String> warehouseIds);
}

