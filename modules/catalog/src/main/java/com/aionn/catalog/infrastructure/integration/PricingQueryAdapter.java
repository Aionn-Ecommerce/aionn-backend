package com.aionn.catalog.infrastructure.integration;

import com.aionn.catalog.application.port.out.ProductRepository;
import com.aionn.catalog.domain.model.Product;
import com.aionn.catalog.domain.model.ProductVariant;
import com.aionn.catalog.domain.valueobject.ProductStatus;
import com.aionn.sharedkernel.integration.port.catalog.PricingQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PricingQueryAdapter implements PricingQueryPort {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<String, SkuPricing> resolvePricing(List<String> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return Map.of();
        }
        Map<String, SkuPricing> result = new LinkedHashMap<>();
        List<Product> products = productRepository.findBySkuIds(skuIds);
        for (Product product : products) {
            boolean active = product.getStatus() == ProductStatus.PUBLISHED;
            for (ProductVariant variant : product.variants()) {
                if (!skuIds.contains(variant.skuId())) {
                    continue;
                }
                String currency = variant.price() == null ? "VND" : variant.price().currency();
                java.math.BigDecimal price = variant.price() == null
                        ? java.math.BigDecimal.ZERO
                        : variant.price().amount();
                result.put(variant.skuId(), new SkuPricing(
                        variant.skuId(), product.getMerchantId(), price, currency, active));
            }
        }
        return result;
    }
}
