package com.aionn.ucp.infrastructure.gateway;

import com.aionn.sharedkernel.integration.port.catalog.CatalogQueryPort;
import com.aionn.ucp.application.dto.catalog.CatalogProductDto;
import com.aionn.ucp.application.dto.envelope.Price;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * UCP-side adapter for catalog queries. Delegates to the shared-kernel
 * {@link CatalogQueryPort}, then maps the transport-neutral
 * {@link CatalogQueryPort.ProductView} into UCP's outward-facing
 * {@link CatalogProductDto}. UCP is responsible for input correlation
 * (matching variants back to the input ids supplied by the agent) — that
 * detail does not belong in the catalog port surface.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogQueryGateway implements com.aionn.ucp.application.port.out.CatalogQueryPort {

    private final CatalogQueryPort catalogQueryPort;

    @Override
    public List<CatalogProductDto> search(com.aionn.ucp.application.port.out.CatalogQueryPort.SearchCriteria criteria) {
        var ports = catalogQueryPort.search(new CatalogQueryPort.SearchCriteria(
                criteria.query(), criteria.limit(),
                criteria.minPriceMinor(), criteria.maxPriceMinor()));
        List<CatalogProductDto> mapped = new ArrayList<>(ports.size());
        for (var v : ports) {
            mapped.add(toDto(v, null, null));
        }
        return mapped;
    }

    @Override
    public com.aionn.ucp.application.port.out.CatalogQueryPort.LookupResult lookup(List<String> ids) {
        var portResult = catalogQueryPort.lookupByProductOrSkuIds(ids);

        // Build correlation: which input id matched which (productId|skuId) tuple,
        // and whether it was an exact-SKU hit or a featured-variant fallback.
        Map<String, String> idToInputId = new java.util.LinkedHashMap<>();
        Map<String, String> idToMatch = new java.util.LinkedHashMap<>();
        var skuInputs = new java.util.HashSet<>(ids);
        for (var p : portResult.products()) {
            boolean matchedAnyVariant = false;
            for (var v : p.variants()) {
                if (skuInputs.contains(v.skuId())) {
                    String key = p.productId() + "|" + v.skuId();
                    idToInputId.put(key, v.skuId());
                    idToMatch.put(key, "exact");
                    matchedAnyVariant = true;
                }
            }
            if (!matchedAnyVariant && skuInputs.contains(p.productId()) && !p.variants().isEmpty()) {
                String featuredSku = p.variants().get(0).skuId();
                String key = p.productId() + "|" + featuredSku;
                idToInputId.put(key, p.productId());
                idToMatch.put(key, "featured");
            }
        }

        List<CatalogProductDto> products = new ArrayList<>();
        for (var v : portResult.products()) {
            products.add(toDto(v, idToInputId, idToMatch));
        }
        return new com.aionn.ucp.application.port.out.CatalogQueryPort.LookupResult(products, portResult.notFound());
    }

    @Override
    public Optional<CatalogProductDto> getProduct(String id, List<com.aionn.ucp.application.port.out.CatalogQueryPort.SelectedOption> selected) {
        return catalogQueryPort.findByProductOrSkuId(id).map(v -> toDto(v, null, null));
    }

    /**
     * Map a transport-neutral product view into UCP's response DTO. The optional
     * correlation maps {@code "{productId}|{skuId}" → input id supplied by the
     * agent} so the response can echo which inputs each variant came from.
     */
    private static CatalogProductDto toDto(
            CatalogQueryPort.ProductView p,
            Map<String, String> inputCorrelation,
            Map<String, String> matchType) {

        List<CatalogProductDto.Variant> variants = new ArrayList<>(p.variants().size());
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        String currency = null;

        for (var v : p.variants()) {
            BigDecimal price = v.price();
            if (price != null) {
                currency = v.currency();
                if (minPrice == null || price.compareTo(minPrice) < 0) minPrice = price;
                if (maxPrice == null || price.compareTo(maxPrice) > 0) maxPrice = price;
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
                String key = p.productId() + "|" + v.skuId();
                String inputId = inputCorrelation.get(key);
                String mt = matchType != null ? matchType.get(key) : null;
                if (inputId != null) {
                    inputs = List.of(new CatalogProductDto.InputCorrelation(inputId, mt));
                }
            }

            variants.add(new CatalogProductDto.Variant(
                    v.skuId(),
                    v.skuId(),
                    v.displayName(),
                    new CatalogProductDto.Description(p.description()),
                    price == null ? null : new Price(price.longValue(), v.currency()),
                    new CatalogProductDto.Availability(v.available()),
                    opts,
                    inputs));
        }

        CatalogProductDto.PriceRange priceRange = null;
        if (minPrice != null) {
            priceRange = new CatalogProductDto.PriceRange(
                    new Price(minPrice.longValue(), currency),
                    new Price(maxPrice.longValue(), currency));
        }

        List<CatalogProductDto.Media> media = p.imageUrls().stream()
                .map(url -> new CatalogProductDto.Media("image", url, null))
                .toList();

        return new CatalogProductDto(
                p.productId(),
                null,
                p.name(),
                new CatalogProductDto.Description(p.description()),
                null,
                List.of(),
                priceRange,
                media,
                null,
                null,
                variants,
                null);
    }
}
