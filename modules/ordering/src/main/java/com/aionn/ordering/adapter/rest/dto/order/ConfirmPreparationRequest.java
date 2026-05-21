package com.aionn.ordering.adapter.rest.dto.order;

import jakarta.validation.constraints.NotBlank;

public record ConfirmPreparationRequest(@NotBlank String merchantId) {
}

