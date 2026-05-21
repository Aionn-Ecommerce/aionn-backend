package com.aionn.shipping.adapter.rest.dto.shipment;

import jakarta.validation.constraints.NotBlank;

public record FetchLabelRequest(@NotBlank String merchantId) {
}

