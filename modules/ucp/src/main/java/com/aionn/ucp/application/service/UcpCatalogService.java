package com.aionn.ucp.application.service;

import com.aionn.ucp.application.dto.catalog.CatalogProductDto;
import com.aionn.ucp.application.dto.catalog.CatalogRequests;
import com.aionn.ucp.application.dto.catalog.CatalogResponses;
import com.aionn.ucp.application.dto.envelope.UcpMessage;
import com.aionn.ucp.application.port.out.CatalogQueryPort;
import com.aionn.ucp.domain.exception.UcpErrorCode;
import com.aionn.ucp.domain.exception.UcpException;
import com.aionn.ucp.domain.model.CapabilityName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UcpCatalogService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;
    private static final int MAX_LOOKUP_BATCH = 50;

    private final CatalogQueryPort catalogQueryPort;
    private final UcpEnvelopeFactory envelopeFactory;

    public CatalogResponses.SearchResponse search(CatalogRequests.SearchRequest request) {
        boolean hasQuery = request != null && request.query() != null && !request.query().isBlank();
        boolean hasFilters = request != null && request.filters() != null
                && (request.filters().categories() != null
                        || request.filters().brands() != null
                        || request.filters().price() != null);
        if (!hasQuery && !hasFilters) {
            throw new UcpException(UcpErrorCode.CATALOG_REQUEST_INVALID,
                    "Search requires a query or at least one filter");
        }

        int limit = clampLimit(request != null && request.pagination() != null ? request.pagination().limit() : null);
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        List<String> categories = null;
        List<String> brands = null;
        if (request != null && request.filters() != null) {
            CatalogRequests.Filters f = request.filters();
            categories = f.categories();
            brands = f.brands();
            if (f.price() != null) {
                if (f.price().min() != null) {
                    minPrice = BigDecimal.valueOf(f.price().min());
                }
                if (f.price().max() != null) {
                    maxPrice = BigDecimal.valueOf(f.price().max());
                }
            }
        }
        CatalogQueryPort.SearchCriteria criteria = new CatalogQueryPort.SearchCriteria(
                hasQuery ? request.query() : null,
                categories, brands, minPrice, maxPrice, limit);

        List<CatalogProductDto> products = catalogQueryPort.search(criteria);
        return new CatalogResponses.SearchResponse(
                envelopeFactory.ok(CapabilityName.CATALOG_SEARCH),
                products,
                new CatalogResponses.Pagination(null, false, (long) products.size()),
                null);
    }

    public CatalogResponses.LookupResponse lookup(CatalogRequests.LookupRequest request) {
        if (request == null || request.ids() == null || request.ids().isEmpty()) {
            throw new UcpException(UcpErrorCode.CATALOG_REQUEST_INVALID,
                    "Lookup requires at least one id");
        }
        if (request.ids().size() > MAX_LOOKUP_BATCH) {
            throw new UcpException(UcpErrorCode.CATALOG_REQUEST_TOO_LARGE,
                    "Batch size " + request.ids().size() + " exceeds " + MAX_LOOKUP_BATCH);
        }
        CatalogQueryPort.LookupResult result = catalogQueryPort.lookup(request.ids());

        List<UcpMessage> messages = null;
        if (result.notFoundIds() != null && !result.notFoundIds().isEmpty()) {
            messages = new ArrayList<>(result.notFoundIds().size());
            for (String missing : result.notFoundIds()) {
                messages.add(UcpMessage.info("not_found", missing));
            }
        }
        return new CatalogResponses.LookupResponse(
                envelopeFactory.ok(CapabilityName.CATALOG_LOOKUP),
                result.products(),
                messages);
    }

    public CatalogResponses.GetProductResponse getProduct(CatalogRequests.GetProductRequest request) {
        if (request == null || request.id() == null || request.id().isBlank()) {
            throw new UcpException(UcpErrorCode.CATALOG_REQUEST_INVALID,
                    "id is required");
        }
        List<CatalogQueryPort.SelectedOption> selected = null;
        if (request.selected() != null) {
            selected = request.selected().stream()
                    .map(s -> new CatalogQueryPort.SelectedOption(s.name(), s.label()))
                    .toList();
        }
        Optional<CatalogProductDto> product = catalogQueryPort.getProduct(request.id(), selected);
        if (product.isEmpty()) {
            return new CatalogResponses.GetProductResponse(
                    envelopeFactory.error(CapabilityName.CATALOG_LOOKUP),
                    null,
                    List.of(UcpMessage.error("not_found",
                            "Product not found: " + request.id(),
                            "unrecoverable")));
        }
        return new CatalogResponses.GetProductResponse(
                envelopeFactory.ok(CapabilityName.CATALOG_LOOKUP),
                product.get(),
                null);
    }

    private static int clampLimit(Integer requested) {
        if (requested == null || requested <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(requested, MAX_LIMIT);
    }
}
