package com.aionn.ordering.infrastructure.gateway;

import com.aionn.ordering.application.port.out.VoucherGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Default voucher adapter: every voucher is treated as a no-op valid voucher
 * with zero discount. Useful for dev/test until the Promotion module is
 * wired in.
 */
@Component
@ConditionalOnProperty(prefix = "ordering.voucher", name = "provider", havingValue = "no-discount", matchIfMissing = true)
public class NoDiscountVoucherGateway implements VoucherGateway {

    @Override
    public Discount apply(String userId, String voucherCode, BigDecimal lineSubtotal, String currency) {
        return new Discount(BigDecimal.ZERO, currency, true, "no-discount provider");
    }
}

