package com.aionn.ordering.adapter.rest.dto.cart;

import jakarta.validation.constraints.Min;

public record UpdateCartItemRequest(@Min(0) int newQty) {
}

