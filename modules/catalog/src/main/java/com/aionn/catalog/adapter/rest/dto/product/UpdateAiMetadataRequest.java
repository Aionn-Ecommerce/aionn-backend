package com.aionn.catalog.adapter.rest.dto.product;

import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateAiMetadataRequest(
        List<String> tags,
        @Size(max = 4000) String aiDescription) {
}

