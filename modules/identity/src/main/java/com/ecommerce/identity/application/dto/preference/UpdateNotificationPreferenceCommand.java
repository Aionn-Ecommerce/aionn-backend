package com.ecommerce.identity.application.dto.preference;

public record UpdateNotificationPreferenceCommand(
        String userId,
        String notificationSettingsJson) {
}
