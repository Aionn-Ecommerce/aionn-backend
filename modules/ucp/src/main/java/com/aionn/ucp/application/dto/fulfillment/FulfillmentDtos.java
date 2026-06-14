package com.aionn.ucp.application.dto.fulfillment;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public final class FulfillmentDtos {

        private FulfillmentDtos() {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Fulfillment(
                        List<FulfillmentMethod> methods,
                        List<AvailableMethod> available_methods) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record FulfillmentMethod(
                        String id,
                        String type,
                        List<String> line_item_ids,
                        String selected_destination_id,
                        List<FulfillmentDestination> destinations,
                        List<FulfillmentGroup> groups) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record FulfillmentDestination(
                        String id,
                        String first_name,
                        String last_name,
                        String street_address,
                        String address_locality,
                        String address_region,
                        String postal_code,
                        String address_country) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record FulfillmentGroup(
                        String id,
                        List<String> line_item_ids,
                        String selected_option_id,
                        List<FulfillmentOption> options) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record FulfillmentOption(
                        String id,
                        String title,
                        String description,
                        List<Total> totals) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Total(String type, long amount) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record AvailableMethod(
                        String type,
                        List<String> line_item_ids,
                        String fulfillable_on,
                        String description) {
        }

        // ── Request-side: fulfillment info sent by platform during update ──

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record FulfillmentRequest(
                        List<FulfillmentMethodRequest> methods) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record FulfillmentMethodRequest(
                        String id,
                        String type,
                        List<String> line_item_ids,
                        String selected_destination_id,
                        List<FulfillmentDestination> destinations,
                        List<FulfillmentGroupRequest> groups) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record FulfillmentGroupRequest(
                        String id,
                        String selected_option_id) {
        }
}
