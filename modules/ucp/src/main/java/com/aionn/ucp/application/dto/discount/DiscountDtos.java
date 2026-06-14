package com.aionn.ucp.application.dto.discount;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public final class DiscountDtos {

        private DiscountDtos() {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record DiscountRequest(List<String> codes) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record DiscountResponse(
                        List<String> codes,
                        List<AppliedDiscount> applied) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record AppliedDiscount(
                        String code,
                        String title,
                        long amount,
                        Boolean automatic,
                        Boolean provisional,
                        String eligibility,
                        Integer priority,
                        String method,
                        List<Allocation> allocations) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Allocation(String path, long amount) {
        }
}
