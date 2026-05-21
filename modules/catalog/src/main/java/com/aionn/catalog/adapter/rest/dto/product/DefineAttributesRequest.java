package com.aionn.catalog.adapter.rest.dto.product;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record DefineAttributesRequest(@NotNull Map<String, String> attributes) {
}

