package com.aionn.catalog.application.dto.search;

import com.aionn.catalog.domain.valueobject.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Read-side filter for {@code GET /catalog/products/search}. Backed by OpenSearch.
 *
 * <p>{@code attributes} carries category-template facet selections (e.g. color=red,
 * size=M). {@code provinceCodes} carries canonical VN GSO codes (e.g. {@code "01"}
 * for Hà Nội) so the FE filter can stay decoupled from the human-readable name
 * stored in identity. When OpenSearch is unreachable or the index is empty the
 * service falls back to a JPA-only path that only honours {@code merchantId}/
 * {@code status}.</p>
 */
public record ProductSearchCriteria(
        String q,
        String merchantId,
        ProductStatus status,
        List<String> categoryIds,
        List<String> brandIds,
        BigDecimal priceMin,
        BigDecimal priceMax,
        Map<String, List<String>> attributes,
        Sort sort,
        int page,
        int size,
        Double ratingMin,
        Boolean onSale,
        List<String> shipping,
        List<String> provinceCodes) {

    public enum Sort {
        RELEVANCE,
        NEWEST,
        PRICE_ASC,
        PRICE_DESC,
        BEST_SELLER
    }

    public ProductSearchCriteria {
        categoryIds = categoryIds == null ? List.of() : List.copyOf(categoryIds);
        brandIds = brandIds == null ? List.of() : List.copyOf(brandIds);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        shipping = shipping == null ? List.of() : List.copyOf(shipping);
        provinceCodes = provinceCodes == null ? List.of() : List.copyOf(provinceCodes);
        if (sort == null) {
            sort = Sort.RELEVANCE;
        }
        if (page < 0) {
            page = 0;
        }
        if (size < 1) {
            size = 20;
        }
        if (size > 100) {
            size = 100;
        }
    }

    public ProductSearchCriteria(
            String q,
            String merchantId,
            ProductStatus status,
            List<String> categoryIds,
            List<String> brandIds,
            BigDecimal priceMin,
            BigDecimal priceMax,
            Map<String, List<String>> attributes,
            Sort sort,
            int page,
            int size) {
        this(q, merchantId, status, categoryIds, brandIds, priceMin, priceMax, attributes, sort, page, size, null, null, List.of(), List.of());
    }

    public boolean hasText() {
        return q != null && !q.isBlank();
    }
}
