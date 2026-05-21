package com.aionn.catalog.adapter.rest.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
        @NotBlank @Size(min = 1, max = 50) String merchantId,
        @NotBlank @Size(min = 1, max = 255) String name) {
}

