package com.aionn.catalog.application.port.out;

import com.aionn.catalog.application.dto.search.ProductSearchCriteria;
import com.aionn.catalog.application.dto.search.ProductSearchDocument;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductSearchIndex {

    void index(ProductSearchDocument document);

    void indexAll(List<ProductSearchDocument> documents);

    void remove(String productId);

    void removeAll(List<String> productIds);

    /**
     * Run a filtered + faceted search.
     *
     * <p>Returns {@link Optional#empty()} when the underlying index is unreachable
     * so callers can fall back to a JPA path. A successful but empty match
     * returns a present {@link SearchHits} with zero hits.</p>
     */
    Optional<SearchHits> search(ProductSearchCriteria criteria);

    record SearchHits(
            List<String> productIds,
            long totalHits,
            Map<String, Long> brandCounts,
            Map<String, Long> categoryCounts,
            Map<String, Map<String, Long>> attributeCounts,
            BigDecimal priceMin,
            BigDecimal priceMax) {
    }
}
