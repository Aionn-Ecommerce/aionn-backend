package com.aionn.inventory.adapter.rest.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ConfigureSafetyStockRequest(
        @NotBlank String merchantId,
        @Min(0) int safetyStockQty) {
}

