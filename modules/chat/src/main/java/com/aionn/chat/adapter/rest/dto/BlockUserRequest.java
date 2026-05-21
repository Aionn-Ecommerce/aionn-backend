package com.aionn.chat.adapter.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record BlockUserRequest(
        @NotBlank String blockedId,
        String reason) {
}

