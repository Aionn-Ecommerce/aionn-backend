package com.aionn.identity.adapter.rest.dto.preference;

import jakarta.validation.constraints.NotBlank;

public record AiPrivacyPreferenceRequest(
        @NotBlank(message = "AI privacy settings JSON is required")
        String aiPrivacySettingsJson) {
}



