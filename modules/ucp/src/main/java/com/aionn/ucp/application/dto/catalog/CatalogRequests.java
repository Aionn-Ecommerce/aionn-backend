package com.aionn.ucp.application.dto.catalog;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class CatalogRequests {

        private CatalogRequests() {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Context(String address_country,
                        String address_region,
                        String language,
                        String currency,
                        String intent) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record PriceFilter(Long min, Long max) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Filters(List<String> categories,
                        List<String> brands,
                        PriceFilter price) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Pagination(Integer limit, String cursor) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record SearchRequest(String query,
                        Context context,
                        Filters filters,
                        Pagination pagination) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record LookupRequest(@Size(min = 1, max = 50) List<String> ids,
                        Context context) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record GetProductRequest(String id,
                        List<SelectedOption> selected,
                        List<String> preferences,
                        Context context) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record SelectedOption(String name, String label) {
        }
}
