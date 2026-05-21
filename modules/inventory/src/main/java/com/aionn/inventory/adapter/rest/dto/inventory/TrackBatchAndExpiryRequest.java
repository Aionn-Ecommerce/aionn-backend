package com.aionn.inventory.adapter.rest.dto.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TrackBatchAndExpiryRequest(
        @NotBlank String merchantId,
        @Size(max = 100) String batchNo,
        LocalDate expiryDate) {
}

