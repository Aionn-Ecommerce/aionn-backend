package com.aionn.identity.adapter.rest.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank(message = "Session ID is required")
        String sessionId) {
}



