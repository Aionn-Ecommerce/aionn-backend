package com.aionn.promotion.application.dto.voucher.result;

import java.math.BigDecimal;
import java.time.Instant;

public record VoucherResult(
        String voucherCode,
        String campaignId,
        BigDecimal discountAmount,
        String currency,
        int usageLimit,
        int usedCount,
        int reservedCount,
        Instant validFrom,
        Instant validUntil,
        Instant createdAt,
        Instant updatedAt) {
}

