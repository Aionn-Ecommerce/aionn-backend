package com.aionn.ordering.application.port.out;

import java.math.BigDecimal;

/** Outbound port for voucher discount preview at order placement. */
public interface VoucherGateway {

    Discount apply(String userId, String voucherCode, BigDecimal lineSubtotal, String currency);

    record Discount(BigDecimal amount, String currency, boolean valid, String reason) {
    }
}
