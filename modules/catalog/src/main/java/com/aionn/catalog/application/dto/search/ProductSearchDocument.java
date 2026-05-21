package com.aionn.catalog.application.dto.search;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Search index document. Field shape is dictated by the AI agent's
 * lexical+semantic search needs: {@code aiDescription} feeds NLP, {@code tags}
 * and filterable attributes power facet filters, {@code priceFrom} drives the
 * "cheapest matching" ranker.
 */
public record ProductSearchDocument(
        String productId,
        String merchantId,
        String name,
        String aiDescription,
        String brandId,
        List<String> categoryIds,
        List<String> collectionIds,
        List<String> tags,
        List<String> imageList,
        Map<String, String> filterableAttributes,
        BigDecimal priceFrom,
        BigDecimal priceTo,
        String currency,
        String status,
        Instant updatedAt) {
}

