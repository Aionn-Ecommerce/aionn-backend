package com.aionn.ordering.infrastructure.gateway;

import com.aionn.inventory.application.port.out.InventoryItemRepository;
import com.aionn.inventory.application.port.out.WarehouseRepository;
import com.aionn.inventory.domain.model.InventoryItem;
import com.aionn.inventory.domain.model.Warehouse;
import com.aionn.ordering.application.port.out.CatalogPricingGateway;
import com.aionn.sharedkernel.integration.port.catalog.PricingQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CatalogPricingPortGateway implements CatalogPricingGateway {

    private final PricingQueryPort pricingQueryPort;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository inventoryItemRepository;

    @Override
    public Map<String, SkuPricing> resolve(List<String> skuIds) {
        Map<String, PricingQueryPort.SkuPricing> pricing = pricingQueryPort.resolvePricing(skuIds);
        Map<String, SkuPricing> result = new LinkedHashMap<>();
        for (Map.Entry<String, PricingQueryPort.SkuPricing> entry : pricing.entrySet()) {
            PricingQueryPort.SkuPricing p = entry.getValue();
            String warehouseId = pickWarehouseWithStock(p.merchantId(), p.skuId());
            result.put(entry.getKey(), new SkuPricing(
                    p.skuId(), p.merchantId(), warehouseId, p.price(), p.currency(), p.active()));
        }
        return result;
    }

    private String pickWarehouseWithStock(String merchantId, String skuId) {
        List<Warehouse> warehouses = warehouseRepository.findByMerchantOrderByPriority(merchantId);
        if (warehouses.isEmpty()) {
            return null;
        }
        List<String> warehouseIds = warehouses.stream().map(Warehouse::getWarehouseId).toList();
        List<InventoryItem> items = inventoryItemRepository.findBySkuAcrossWarehouses(skuId, warehouseIds);
        for (Warehouse w : warehouses) {
            for (InventoryItem item : items) {
                if (item.getKey().warehouseId().equals(w.getWarehouseId()) && item.getAvailableQty() > 0) {
                    return w.getWarehouseId();
                }
            }
        }
        return warehouses.get(0).getWarehouseId();
    }
}
