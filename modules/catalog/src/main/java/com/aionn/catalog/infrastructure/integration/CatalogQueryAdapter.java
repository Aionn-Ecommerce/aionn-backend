package com.aionn.catalog.infrastructure.integration;

import com.aionn.catalog.application.port.out.ProductPersistencePort;
import com.aionn.catalog.domain.model.Product;
import com.aionn.catalog.domain.model.ProductVariant;
import com.aionn.catalog.domain.valueobject.ProductStatus;
import com.aionn.sharedkernel.domain.vo.Money;
import com.aionn.sharedkernel.integration.port.catalog.CatalogQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Catalog-side adapter that exposes product views to other services without
 * leaking domain types. UCP, search aggregators, and any future caller all
 * consume {@link ProductView} records — no {@code Product} entity ever
 * crosses the bounded-context boundary.
 *
 * <p>Filtering rules (taken-down hidden, price-range applied, taken from
 * variants) live here because they are cataloged business rules. The caller
 * just consumes the result.
 */
@Component
@RequiredArgsConstructor
public class CatalogQueryAdapter implements CatalogQueryPort {

    private final ProductPersistencePort productRepository;

    @Override
    public List<ProductView> search(SearchCriteria criteria) {
        List<Product> products = productRepository.searchPublished(criteria.query(), criteria.limit(), 0);
        List<ProductView> mapped = new ArrayList<>(products.size());
        for (Product p : products) {
            ProductView dto = toView(p);
            if (dto != null && matchesPrice(dto, criteria.minPriceMinor(), criteria.maxPriceMinor())) {
                mapped.add(dto);
            }
        }
        return mapped;
    }

    @Override
    public Optional<ProductView> findByProductOrSkuId(String id) {
        Optional<Product> byId = productRepository.findById(id);
        if (byId.isPresent()) {
            return Optional.ofNullable(toView(byId.get()));
        }
        List<Product> bySku = productRepository.findBySkuIds(List.of(id));
        if (!bySku.isEmpty()) {
            return Optional.ofNullable(toView(bySku.get(0)));
        }
        return Optional.empty();
    }

    @Override
    public LookupResult lookupByProductOrSkuIds(List<String> ids) {
        // Resolve any input matching a SKU first.
        List<Product> bySku = productRepository.findBySkuIds(ids);
        Map<String, Product> productById = new LinkedHashMap<>();
        Set<String> resolvedInputIds = new HashSet<>();
        for (Product p : bySku) {
            productById.put(p.getProductId(), p);
            for (ProductVariant v : p.variants()) {
                if (ids.contains(v.skuId())) {
                    resolvedInputIds.add(v.skuId());
                }
            }
        }
        // Fall back to product-id resolution for everything not covered by a SKU.
        for (String id : ids) {
            if (resolvedInputIds.contains(id) || productById.containsKey(id)) {
                continue;
            }
            productRepository.findById(id).ifPresent(p -> {
                productById.put(p.getProductId(), p);
                resolvedInputIds.add(id);
            });
        }

        List<ProductView> views = new ArrayList<>();
        for (Product p : productById.values()) {
            ProductView v = toView(p);
            if (v != null) {
                views.add(v);
            }
        }
        List<String> notFound = ids.stream().filter(id -> !resolvedInputIds.contains(id)).toList();
        return new LookupResult(views, notFound);
    }

    private static boolean matchesPrice(ProductView dto, BigDecimal minMinor, BigDecimal maxMinor) {
        if (minMinor == null && maxMinor == null) {
            return true;
        }
        if (dto.variants().isEmpty()) {
            return true;
        }
        BigDecimal cheapest = dto.variants().stream()
                .map(VariantView::price)
                .filter(p -> p != null)
                .min(BigDecimal::compareTo)
                .orElse(null);
        if (cheapest == null) {
            return true;
        }
        if (minMinor != null && cheapest.compareTo(minMinor) < 0) {
            return false;
        }
        if (maxMinor != null && cheapest.compareTo(maxMinor) > 0) {
            return false;
        }
        return true;
    }

    private static ProductView toView(Product p) {
        if (p.getStatus() == ProductStatus.TAKEN_DOWN) {
            return null;
        }
        List<ProductVariant> domainVariants = p.variants();
        List<VariantView> variants = new ArrayList<>(domainVariants.size());
        for (ProductVariant v : domainVariants) {
            Money price = v.price();
            BigDecimal amount = price == null ? null : price.amount();
            String currency = price == null ? null : price.currency();
            variants.add(new VariantView(
                    v.skuId(),
                    p.getName() + variantSuffix(v),
                    amount,
                    currency,
                    price != null,
                    v.attributeValues() == null ? Map.of() : v.attributeValues()));
        }
        return new ProductView(
                p.getProductId(),
                p.getName(),
                p.getAiDescription(),
                p.imageList(),
                variants);
    }

    private static String variantSuffix(ProductVariant v) {
        if (v.attributeValues() == null || v.attributeValues().isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(" (");
        boolean first = true;
        for (Map.Entry<String, String> e : v.attributeValues().entrySet()) {
            if (!first) sb.append(", ");
            sb.append(e.getValue());
            first = false;
        }
        sb.append(')');
        return sb.toString();
    }
}
