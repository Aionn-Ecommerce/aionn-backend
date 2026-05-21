package com.aionn.ordering.adapter.rest.dto.order;

import jakarta.validation.constraints.NotBlank;

public record ConfirmShippedRequest(@NotBlank String shipmentId) {
}

