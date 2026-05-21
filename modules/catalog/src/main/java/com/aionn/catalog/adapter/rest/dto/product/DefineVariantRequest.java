package com.aionn.catalog.adapter.rest.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Map;

public record DefineVariantRequest(
        @NotEmpty Map<String, String> attributeValues,
        @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
        @Size(min = 3, max = 3) String currency) {
}

