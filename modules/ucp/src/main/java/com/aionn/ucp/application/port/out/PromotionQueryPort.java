package com.aionn.ucp.application.port.out;

import java.util.Optional;

public interface PromotionQueryPort {

    Optional<DiscountInfo> validateCode(String code, String userId, long orderTotalMinor, String currency);

    record DiscountInfo(
            String code,
            String title,
            long discountAmountMinor,
            String currency,
            String rejectionReason) {

        public boolean isValid() {
            return rejectionReason == null;
        }
    }
}
