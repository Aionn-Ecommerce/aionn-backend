package com.aionn.ucp.adapter.rest.dto.cart.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record UcpAddCartItemRequest(
        @NotBlank String skuId,
        @Positive int qty) {
}
