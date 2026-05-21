package com.aionn.inventory.application.port.out;

/**
 * Notifies a merchant when a SKU at one of their warehouses dips below the
 * configured safety stock threshold. Implementations follow the project's
 * 2-impl pattern: a logging mock and a remote stub.
 */
public interface SafetyStockNotifier {

    void notifySafetyStockBreach(
            String merchantId,
            String skuId,
            String warehouseId,
            int availableQty,
            int safetyStockQty);
}

