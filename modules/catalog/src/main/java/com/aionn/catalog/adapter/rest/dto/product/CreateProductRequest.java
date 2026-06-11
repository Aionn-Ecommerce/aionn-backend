package com.aionn.catalog.adapter.rest.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * The merchant is derived from the authenticated user, not the request body,
 * so callers cannot create products under a foreign merchant id.
 */
public record CreateProductRequest(
                @NotBlank @Size(min = 1, max = 255) String name) {
}
