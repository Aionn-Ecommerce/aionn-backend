package com.ecommerce.identity.application.dto.user;

public record UserActionOutcomeView(
        String action,
        String message,
        UserProfileView profile) {
}
