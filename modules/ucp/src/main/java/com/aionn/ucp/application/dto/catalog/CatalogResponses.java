package com.aionn.ucp.application.dto.catalog;

import com.aionn.ucp.application.dto.envelope.UcpEnvelope;
import com.aionn.ucp.application.dto.envelope.UcpMessage;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class CatalogResponses {

        private CatalogResponses() {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record SearchResponse(
                        @JsonProperty("ucp") UcpEnvelope ucp,
                        List<CatalogProductDto> products,
                        Pagination pagination,
                        List<UcpMessage> messages) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record LookupResponse(
                        @JsonProperty("ucp") UcpEnvelope ucp,
                        List<CatalogProductDto> products,
                        List<UcpMessage> messages) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record GetProductResponse(
                        @JsonProperty("ucp") UcpEnvelope ucp,
                        CatalogProductDto product,
                        List<UcpMessage> messages) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Pagination(String cursor, Boolean has_next_page, Long total_count) {
        }
}
