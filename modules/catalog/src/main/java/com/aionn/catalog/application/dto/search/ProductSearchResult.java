package com.aionn.catalog.application.dto.search;

import com.aionn.catalog.application.dto.common.PageResult;
import com.aionn.catalog.application.dto.product.result.ProductResult;

import java.util.List;
import java.util.Map;

/**
 * Search response. Wraps the regular product page plus aggregations needed
 * by the storefront sidebar (brand counts, category counts, attribute facet
 * counts, price min/max bounds).
 */
public record ProductSearchResult(
        PageResult<ProductResult> page,
        Facets facets) {

    public record Facets(
            Map<String, Long> brands,
            Map<String, Long> categories,
            Map<String, Map<String, Long>> attributes,
            PriceRange priceRange) {

        public static Facets empty() {
            return new Facets(Map.of(), Map.of(), Map.of(), null);
        }
    }

    public record PriceRange(java.math.BigDecimal min, java.math.BigDecimal max) {
    }

    public static ProductSearchResult of(PageResult<ProductResult> page) {
        return new ProductSearchResult(page, Facets.empty());
    }

    public static ProductSearchResult of(List<ProductResult> content, int page, int size, long totalElements, Facets facets) {
        return new ProductSearchResult(new PageResult<>(content, page, size, totalElements), facets);
    }
}
