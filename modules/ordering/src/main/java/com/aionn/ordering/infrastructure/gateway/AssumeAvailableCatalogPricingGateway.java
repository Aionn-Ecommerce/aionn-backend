package com.aionn.ordering.infrastructure.gateway;

import com.aionn.ordering.application.port.out.CatalogPricingGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default catalog pricing adapter for dev/test. Resolves every SKU to a
 * configured fallback merchant + warehouse + flat price. Replace with the
 * real catalog query (or remote gateway) when running real flows.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "ordering.catalog-pricing", name = "provider", havingValue = "assume-available", matchIfMissing = true)
public class AssumeAvailableCatalogPricingGateway implements CatalogPricingGateway {

    @Value("${ordering.catalog-pricing.fallback-merchant-id:test-merchant}")
    private String fallbackMerchantId;

    @Value("${ordering.catalog-pricing.fallback-warehouse-id:test-warehouse}")
    private String fallbackWarehouseId;

    @Value("${ordering.catalog-pricing.fallback-price:99000}")
    private BigDecimal fallbackPrice;

    @Value("${ordering.catalog-pricing.fallback-currency:VND}")
    private String fallbackCurrency;

    @Override
    public Map<String, SkuPricing> resolve(List<String> skuIds) {
        Map<String, SkuPricing> map = new LinkedHashMap<>();
        for (String skuId : skuIds) {
            map.put(skuId, new SkuPricing(skuId, fallbackMerchantId, fallbackWarehouseId,
                    fallbackPrice, fallbackCurrency, true));
        }
        log.debug("[ASSUME-AVAILABLE] resolved {} SKUs", skuIds.size());
        return map;
    }
}

