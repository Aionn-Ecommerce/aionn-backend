package com.aionn.payment.adapter.rest.dto.payout;

import jakarta.validation.constraints.NotBlank;

public record PayoutFailBody(@NotBlank String reason) {
}
