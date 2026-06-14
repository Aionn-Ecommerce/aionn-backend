package com.aionn.ucp.application.dto.cart;

import com.aionn.ucp.application.dto.checkout.CheckoutDtos;
import com.aionn.ucp.application.dto.envelope.UcpEnvelope;
import com.aionn.ucp.application.dto.envelope.UcpMessage;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CartDtos {

        private CartDtos() {
        }

        @Schema(description = "Create cart request")
        public record CreateRequest(
                        List<CheckoutDtos.CreateLineItem> line_items,
                        CheckoutDtos.Buyer buyer,
                        CheckoutDtos.Context context) {
        }

        @Schema(description = "Update cart request - full replacement")
        public record UpdateRequest(
                        String id,
                        List<CheckoutDtos.CreateLineItem> line_items,
                        CheckoutDtos.Buyer buyer,
                        CheckoutDtos.Context context) {
        }

        @Schema(description = "Cart response per UCP spec")
        public record CartResponse(
                        UcpEnvelope ucp,
                        String id,
                        String currency,
                        List<CheckoutDtos.CheckoutLineItem> line_items,
                        CheckoutDtos.Buyer buyer,
                        CheckoutDtos.Context context,
                        List<CheckoutDtos.Total> totals,
                        List<CheckoutDtos.Link> links,
                        List<UcpMessage> messages,
                        String continue_url,
                        String expires_at) {

                public static CartResponse error(UcpEnvelope errorEnvelope, List<UcpMessage> messages) {
                        return new CartResponse(
                                        errorEnvelope,
                                        null, null, null, null, null, null, null,
                                        messages, null, null);
                }
        }
}
