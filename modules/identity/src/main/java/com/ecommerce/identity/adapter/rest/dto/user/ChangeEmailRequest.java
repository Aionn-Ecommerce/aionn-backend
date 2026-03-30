package com.ecommerce.identity.adapter.rest.dto.user;

import jakarta.validation.constraints.NotNull;

public record ChangeEmailRequest(
        @NotNull(message = "Action is required")
        OtpAction action,
        String newEmail,
        String otpCode) {
}
