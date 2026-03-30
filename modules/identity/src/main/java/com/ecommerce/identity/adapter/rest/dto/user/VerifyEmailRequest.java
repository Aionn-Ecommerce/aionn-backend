package com.ecommerce.identity.adapter.rest.dto.user;

import jakarta.validation.constraints.NotNull;

public record VerifyEmailRequest(
        @NotNull(message = "Action is required")
        OtpAction action,
        String otpCode) {
}
