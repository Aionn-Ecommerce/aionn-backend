package com.aionn.inventory.adapter.rest.dto.transfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CompleteTransferRequest(
        @NotBlank String merchantId,
        @Positive int receivedQty) {
}

