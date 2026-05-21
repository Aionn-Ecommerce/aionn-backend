package com.aionn.chat.adapter.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record StartConversationRequest(
        @NotBlank String merchantId,
        String buyerDisplayName,
        String buyerAvatarUrl,
        String merchantDisplayName,
        String merchantAvatarUrl) {
}

