package com.aionn.identity.adapter.rest.dto.user.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeAvatarRequest(
        @NotBlank(message = "Avatar URL is required")
        String avatarUrl) {
}


