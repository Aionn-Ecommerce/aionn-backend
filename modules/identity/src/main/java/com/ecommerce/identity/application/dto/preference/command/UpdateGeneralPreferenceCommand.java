package com.ecommerce.identity.application.dto.preference.command;

public record UpdateGeneralPreferenceCommand(
                String userId,
                String language,
                String currency,
                String timezone,
                String theme) {
}
