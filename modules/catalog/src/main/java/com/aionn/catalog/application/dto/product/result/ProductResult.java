package com.aionn.catalog.application.dto.product.result;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ProductResult(
        String productId,
        String merchantId,
        String name,
        String brandId,
        List<String> categoryIds,
        List<String> imageList,
        List<String> tags,
        List<String> collectionIds,
        Map<String, String> attributes,
        List<VariantResult> variants,
        String aiDescription,
        String status,
        Instant createdAt,
        Instant updatedAt) {

    public record VariantResult(
            String skuId,
            Map<String, String> attributeValues,
            BigDecimal price,
            String currency) {
    }
}

