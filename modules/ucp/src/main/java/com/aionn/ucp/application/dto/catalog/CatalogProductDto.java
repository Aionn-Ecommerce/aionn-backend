package com.aionn.ucp.application.dto.catalog;

import com.aionn.ucp.application.dto.envelope.Price;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CatalogProductDto(
                String id,
                String handle,
                String title,
                Description description,
                String url,
                List<CategoryRef> categories,
                PriceRange price_range,
                List<Media> media,
                List<ProductOption> options,
                List<SelectedOption> selected,
                List<Variant> variants,
                Rating rating) {

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Description(String plain) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record CategoryRef(String value, String taxonomy) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record PriceRange(Price min, Price max) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Media(String type, String url, String alt_text) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record ProductOption(String name, List<OptionValue> values) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record OptionValue(String label, Boolean available, Boolean exists) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record SelectedOption(String name, String label) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Variant(
                        String id,
                        String sku,
                        String title,
                        Description description,
                        Price price,
                        Availability availability,
                        List<SelectedOption> options,
                        List<InputCorrelation> inputs) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Availability(boolean available) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record InputCorrelation(String id, String match) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Rating(Double value, Integer scale_max, Integer count) {
        }
}
