package com.aionn.ucp.infrastructure.gateway;

import com.aionn.catalog.application.port.out.ProductPersistencePort;
import com.aionn.catalog.domain.model.Product;
import com.aionn.catalog.domain.model.ProductVariant;
import com.aionn.catalog.domain.valueobject.ProductStatus;
import com.aionn.sharedkernel.domain.vo.Money;
import com.aionn.ucp.application.dto.catalog.CatalogProductDto;
import com.aionn.ucp.application.dto.envelope.Price;
import com.aionn.ucp.application.port.out.CatalogQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogQueryGateway implements CatalogQueryPort {

    private final ProductPersistencePort productRepository;

    @Override
    public List<CatalogProductDto> search(SearchCriteria criteria) {
        List<Product> products = productRepository.searchPublished(criteria.query(), criteria.limit());
        List<CatalogProductDto> mapped = new ArrayList<>(products.size());
        for (Product p : products) {
            CatalogProductDto dto = mapProduct(p, null, null);
            if (dto != null && matchesFilters(dto, criteria)) {
                mapped.add(dto);
            }
        }
        return mapped;
    }

    @Override
    public LookupResult lookup(List<String> ids) {
        // ids may be product ids OR sku ids — try both.
        List<Product> bySku = productRepository.findBySkuIds(ids);
        Map<String, Product> productById = new LinkedHashMap<>();
        Map<String, String> idToMatch = new LinkedHashMap<>();
        Map<String, String> idToInputId = new LinkedHashMap<>();

        for (Product p : bySku) {
            productById.put(p.getProductId(), p);
            for (ProductVariant v : p.variants()) {
                if (ids.contains(v.skuId())) {
                    idToMatch.put(p.getProductId() + "|" + v.skuId(), "exact");
                    idToInputId.put(p.getProductId() + "|" + v.skuId(), v.skuId());
                }
            }
        }
        // Also try product-id resolution for any remaining ids not covered by sku
        // match.
        Set<String> resolvedSkus = new HashSet<>();
        bySku.forEach(p -> p.variants().forEach(v -> {
            if (ids.contains(v.skuId()))
                resolvedSkus.add(v.skuId());
        }));
        for (String id : ids) {
            if (resolvedSkus.contains(id))
                continue;
            Optional<Product> found = productRepository.findById(id);
            found.ifPresent(p -> {
                productById.put(p.getProductId(), p);
                if (!p.variants().isEmpty()) {
                    String featuredSku = p.variants().get(0).skuId();
                    idToMatch.put(p.getProductId() + "|" + featuredSku, "featured");
                    idToInputId.put(p.getProductId() + "|" + featuredSku, id);
                }
            });
        }

        List<CatalogProductDto> products = new ArrayList<>();
        Set<String> producedFor = new HashSet<>();
        for (Product p : productById.values()) {
            CatalogProductDto dto = mapProduct(p, idToInputId, idToMatch);
            if (dto != null) {
                products.add(dto);
                producedFor.addAll(idToInputId.values().stream()
                        .filter(input -> ids.contains(input))
                        .toList());
            }
        }
        List<String> notFound = ids.stream().filter(id -> !producedFor.contains(id)).toList();
        return new LookupResult(products, notFound);
    }

    @Override
    public Optional<CatalogProductDto> getProduct(String id, List<SelectedOption> selected) {
        Optional<Product> byId = productRepository.findById(id);
        if (byId.isPresent()) {
            return Optional.ofNullable(mapProduct(byId.get(), null, null));
        }
        // Fallback: lookup by SKU.
        List<Product> bySku = productRepository.findBySkuIds(List.of(id));
        if (!bySku.isEmpty()) {
            return Optional.ofNullable(mapProduct(bySku.get(0), null, null));
        }
        return Optional.empty();
    }

    private static boolean matchesFilters(CatalogProductDto dto, SearchCriteria criteria) {
        if (criteria.minPriceMinor() == null && criteria.maxPriceMinor() == null) {
            return true;
        }
        if (dto.price_range() == null || dto.price_range().min() == null) {
            return true;
        }
        long min = dto.price_range().min().amount();
        if (criteria.minPriceMinor() != null && BigDecimal.valueOf(min).compareTo(criteria.minPriceMinor()) < 0) {
            return false;
        }
        long max = dto.price_range().max() == null ? min : dto.price_range().max().amount();
        if (criteria.maxPriceMinor() != null && BigDecimal.valueOf(max).compareTo(criteria.maxPriceMinor()) > 0) {
            return false;
        }
        return true;
    }

    /**
     * Map domain product to UCP DTO. inputCorrelation maps "{productId}|{skuId}" →
     * input id from request,
     * matchType maps the same key → "exact"/"featured" — both are null when called
     * from search/getProduct.
     */
    private static CatalogProductDto mapProduct(
            Product p,
            Map<String, String> inputCorrelation,
            Map<String, String> matchType) {

        if (p.getStatus() == ProductStatus.TAKEN_DOWN) {
            return null;
        }
        // Variants: at least one variant is required for a product to be purchasable.
        List<ProductVariant> domainVariants = p.variants();
        List<CatalogProductDto.Variant> variants = new ArrayList<>(domainVariants.size());
        Long minMinor = null;
        Long maxMinor = null;
        String currency = null;

        for (ProductVariant v : domainVariants) {
            Money price = v.price();
            Long unitMinor = price == null ? null : price.amount().longValue();
            if (price != null) {
                currency = price.currency();
                if (minMinor == null || unitMinor < minMinor)
                    minMinor = unitMinor;
                if (maxMinor == null || unitMinor > maxMinor)
                    maxMinor = unitMinor;
            }

            List<CatalogProductDto.SelectedOption> opts = null;
            if (v.attributeValues() != null && !v.attributeValues().isEmpty()) {
                opts = new ArrayList<>(v.attributeValues().size());
                for (Map.Entry<String, String> e : v.attributeValues().entrySet()) {
                    opts.add(new CatalogProductDto.SelectedOption(e.getKey(), e.getValue()));
                }
            }

            List<CatalogProductDto.InputCorrelation> inputs = null;
            if (inputCorrelation != null) {
                String key = p.getProductId() + "|" + v.skuId();
                String inputId = inputCorrelation.get(key);
                String mt = matchType != null ? matchType.get(key) : null;
                if (inputId != null) {
                    inputs = List.of(new CatalogProductDto.InputCorrelation(inputId, mt));
                }
            }

            variants.add(new CatalogProductDto.Variant(
                    v.skuId(),
                    v.skuId(),
                    p.getName() + variantSuffix(v),
                    new CatalogProductDto.Description(p.getAiDescription()),
                    price == null ? null : new Price(unitMinor, currency),
                    new CatalogProductDto.Availability(price != null),
                    opts,
                    inputs));
        }

        CatalogProductDto.PriceRange priceRange = null;
        if (minMinor != null) {
            priceRange = new CatalogProductDto.PriceRange(
                    new Price(minMinor, currency),
                    new Price(maxMinor, currency));
        }

        List<CatalogProductDto.Media> media = p.imageList().stream()
                .map(url -> new CatalogProductDto.Media("image", url, null))
                .toList();

        return new CatalogProductDto(
                p.getProductId(),
                null,
                p.getName(),
                new CatalogProductDto.Description(p.getAiDescription()),
                null,
                List.of(),
                priceRange,
                media,
                null,
                null,
                variants,
                null);
    }

    private static String variantSuffix(ProductVariant v) {
        if (v.attributeValues() == null || v.attributeValues().isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(" (");
        boolean first = true;
        for (Map.Entry<String, String> e : v.attributeValues().entrySet()) {
            if (!first)
                sb.append(", ");
            sb.append(e.getValue());
            first = false;
        }
        sb.append(')');
        return sb.toString();
    }
}
