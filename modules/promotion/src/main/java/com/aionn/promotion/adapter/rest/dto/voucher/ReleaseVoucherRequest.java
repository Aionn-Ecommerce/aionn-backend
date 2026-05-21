package com.aionn.promotion.adapter.rest.dto.voucher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReleaseVoucherRequest(
        @NotBlank String userId,
        @NotBlank String orderId,
        @NotBlank @Size(max = 500) String reason) {
}

