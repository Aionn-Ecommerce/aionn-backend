package com.aionn.catalog.adapter.rest.dto.product;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignCollectionsRequest(@NotNull List<String> collectionIds) {
}

