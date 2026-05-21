package com.aionn.catalog.adapter.rest.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ChangeVariantPriceRequest(
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal newPrice,
        @Size(min = 3, max = 3) String currency) {
}

