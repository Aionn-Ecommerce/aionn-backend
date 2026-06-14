package com.aionn.ucp.application.dto.order;

import com.aionn.ucp.application.dto.envelope.UcpEnvelope;
import com.aionn.ucp.application.dto.envelope.UcpMessage;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OrderItem(String id, String title, long price) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Quantity(Integer total, Integer fulfilled) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Total(String type, long amount) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OrderLineItem(
            String id,
            OrderItem item,
            Quantity quantity,
            List<Total> totals,
            String status) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record FulfillmentEvent(
            String id,
            Instant occurred_at,
            String type,
            List<LineItemRef> line_items,
            String tracking_number,
            String tracking_url,
            String description) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record LineItemRef(String id, Integer quantity) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Fulfillment(List<FulfillmentEvent> events) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OrderResponse(
            @JsonProperty("ucp") UcpEnvelope ucp,
            String id,
            String checkout_id,
            String permalink_url,
            String currency,
            String status,
            List<OrderLineItem> line_items,
            Fulfillment fulfillment,
            List<Total> totals,
            List<UcpMessage> messages) {

        public static OrderResponse error(UcpEnvelope env, List<UcpMessage> messages) {
            return new OrderResponse(env, null, null, null, null, null, null, null, null, messages);
        }
    }
}
