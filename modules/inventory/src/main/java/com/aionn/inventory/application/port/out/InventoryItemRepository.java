package com.aionn.inventory.application.port.out;

import com.aionn.inventory.domain.model.InventoryItem;
import com.aionn.inventory.domain.valueobject.InventoryItemKey;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository {

    InventoryItem save(InventoryItem item);

    Optional<InventoryItem> findByKey(InventoryItemKey key);

    /**
     * Pessimistic find used by reserve/commit/release paths to serialize
     * concurrent qty mutations on the same row.
     */
    Optional<InventoryItem> lockByKey(InventoryItemKey key);

    /**
     * Items belonging to the warehouses provided, ordered to suit fulfillment
     * selection.
     */
    List<InventoryItem> findBySkuAcrossWarehouses(String skuId, List<String> warehouseIds);
}

