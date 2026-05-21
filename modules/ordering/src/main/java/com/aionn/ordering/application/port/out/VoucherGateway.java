package com.aionn.ordering.application.port.out;

import java.math.BigDecimal;

/**
 * Outbound port for promotion / voucher application. Default impl returns
 * "no discount" so dev/test can run without a Promotion module.
 */
public interface VoucherGateway {

    Discount apply(String userId, String voucherCode, BigDecimal lineSubtotal, String currency);

    record Discount(BigDecimal amount, String currency, boolean valid, String reason) {
    }
}

