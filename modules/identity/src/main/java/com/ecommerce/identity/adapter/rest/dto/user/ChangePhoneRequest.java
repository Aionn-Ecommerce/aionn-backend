package com.ecommerce.identity.adapter.rest.dto.user;

import jakarta.validation.constraints.NotNull;

public record ChangePhoneRequest(
        @NotNull(message = "Action is required")
        OtpAction action,
        String newPhone,
        String otpCode) {
}
