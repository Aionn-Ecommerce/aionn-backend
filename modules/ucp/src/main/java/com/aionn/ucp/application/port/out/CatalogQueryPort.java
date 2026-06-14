package com.aionn.ucp.application.port.out;

import com.aionn.ucp.application.dto.catalog.CatalogProductDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CatalogQueryPort {

    List<CatalogProductDto> search(SearchCriteria criteria);

    LookupResult lookup(List<String> ids);

    Optional<CatalogProductDto> getProduct(String id, List<SelectedOption> selected);

    record SearchCriteria(
            String query,
            List<String> categoryIds,
            List<String> brandIds,
            BigDecimal minPriceMinor,
            BigDecimal maxPriceMinor,
            int limit) {
    }

    record SelectedOption(String name, String label) {
    }

    record LookupResult(List<CatalogProductDto> products, List<String> notFoundIds) {
    }
}
