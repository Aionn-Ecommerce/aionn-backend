package com.aionn.sharedkernel.integration.port.promotion;

import java.math.BigDecimal;

public interface VoucherApplyPort {

    Discount apply(String userId, String voucherCode, BigDecimal lineSubtotal, String currency);

    record Discount(BigDecimal amount, String currency, boolean applied, String reason) {
    }
}
