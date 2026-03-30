package com.ecommerce.identity.adapter.rest.dto.preference;

public record GeneralPreferenceRequest(
        String language,
        String currency,
        String timezone,
        String theme) {
}
