package com.aionn.ucp.application.dto.checkout;

import com.aionn.ucp.application.dto.discount.DiscountDtos;
import com.aionn.ucp.application.dto.envelope.Price;
import com.aionn.ucp.application.dto.envelope.UcpEnvelope;
import com.aionn.ucp.application.dto.envelope.UcpMessage;
import com.aionn.ucp.application.dto.fulfillment.FulfillmentDtos;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class CheckoutDtos {

        private CheckoutDtos() {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record LineItemRef(String id) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record CreateLineItem(LineItemRef item, int quantity) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record CreateRequest(String cart_id,
                        List<CreateLineItem> line_items,
                        Buyer buyer,
                        FulfillmentDtos.FulfillmentRequest fulfillment,
                        DiscountDtos.DiscountRequest discounts,
                        Context context,
                        String webhook_url) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record UpdateRequest(
                        List<CreateLineItem> line_items,
                        Buyer buyer,
                        FulfillmentDtos.FulfillmentRequest fulfillment,
                        DiscountDtos.DiscountRequest discounts,
                        Context context,
                        Payment payment) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Buyer(String email, String first_name, String last_name, String phone) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Context(
                        String locale,
                        String country,
                        List<String> eligibility) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record FulfillmentRequest(String address_id) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record CompleteRequest(String payment_method_id, String address_id) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record CheckoutItem(String id, String title, long price, String image_url) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record CheckoutLineItem(String id, CheckoutItem item, int quantity, List<Total> totals) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Total(String type, long amount, String display_text) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Link(String type, String url, String title) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Payment(List<PaymentInstrument> instruments) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record PaymentInstrument(String type, String provider) {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record CheckoutResponse(
                        @JsonProperty("ucp") UcpEnvelope ucp,
                        String id,
                        String status,
                        String currency,
                        List<CheckoutLineItem> line_items,
                        Buyer buyer,
                        FulfillmentDtos.Fulfillment fulfillment,
                        DiscountDtos.DiscountResponse discounts,
                        List<Total> totals,
                        List<Link> links,
                        List<UcpMessage> messages,
                        String continue_url,
                        String order_id,
                        String expires_at) {

                public static CheckoutResponse error(UcpEnvelope env, List<UcpMessage> messages, String continueUrl) {
                        return new CheckoutResponse(env, null, null, null, null, null,
                                        null, null, null, null, messages, continueUrl, null, null);
                }
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record CheckoutItemPrice(Price price) {
        }
}
