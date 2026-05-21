package com.aionn.inventory.adapter.rest.dto.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWarehouseRequest(
        @Size(max = 1000) String address,
        @NotBlank String merchantId,
        @Min(0) int priorityLevel) {
}

