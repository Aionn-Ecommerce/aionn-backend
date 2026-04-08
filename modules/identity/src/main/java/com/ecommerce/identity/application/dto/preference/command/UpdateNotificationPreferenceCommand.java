package com.ecommerce.identity.application.dto.preference.command;

public record UpdateNotificationPreferenceCommand(
                String userId,
                String notificationSettingsJson) {
}
