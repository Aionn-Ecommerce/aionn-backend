package com.aionn.catalog.adapter.rest.dto.review;

import jakarta.validation.constraints.NotBlank;

public record MerchantReplyRequest(
        @NotBlank String content) {
}
