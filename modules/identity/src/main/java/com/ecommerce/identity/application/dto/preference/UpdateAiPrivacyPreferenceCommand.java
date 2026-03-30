package com.ecommerce.identity.application.dto.preference;

public record UpdateAiPrivacyPreferenceCommand(
        String userId,
        String aiPrivacySettingsJson) {
}
