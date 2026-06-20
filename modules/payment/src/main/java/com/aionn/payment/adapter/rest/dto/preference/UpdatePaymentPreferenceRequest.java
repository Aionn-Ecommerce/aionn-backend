package com.aionn.payment.adapter.rest.dto.preference;

import jakarta.validation.constraints.NotBlank;

public record UpdatePaymentPreferenceRequest(
        @NotBlank String paymentType,
        String paymentMethodId) {
}
