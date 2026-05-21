package com.aionn.ordering.application.port.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Outbound port to Catalog. Resolves SKU price + active merchant + warehouse
 * picking for the cart at the moment of placing an order. This is what
 * UC5.7 ("Kiá»ƒm tra tÃ­nh há»£p lá»‡ Ä‘Æ¡n hÃ ng") needs: AI agent / system must
 * confirm the SKU is still being sold, at what price, by which merchant,
 * and from which warehouse.
 */
public interface CatalogPricingGateway {

    Map<String, SkuPricing> resolve(List<String> skuIds);

    record SkuPricing(
            String skuId,
            String merchantId,
            String warehouseId,
            BigDecimal price,
            String currency,
            boolean active) {
    }
}

