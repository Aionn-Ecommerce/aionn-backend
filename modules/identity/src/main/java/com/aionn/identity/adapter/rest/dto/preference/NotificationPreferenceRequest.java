package com.aionn.identity.adapter.rest.dto.preference;

import jakarta.validation.constraints.NotBlank;

public record NotificationPreferenceRequest(
        @NotBlank(message = "Notification settings JSON is required")
        String notificationSettingsJson) {
}



