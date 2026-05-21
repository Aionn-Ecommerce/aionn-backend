package com.aionn.catalog.adapter.rest.dto.product;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateMediaRequest(@NotNull List<String> imageList) {
}

