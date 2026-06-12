package com.aionn.promotion.infrastructure.integration;

import com.aionn.promotion.application.port.out.VoucherRepository;
import com.aionn.promotion.domain.model.Voucher;
import com.aionn.sharedkernel.integration.port.promotion.VoucherApplyPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Read-only discount preview. Reservation/commit lifecycle is owned by
 * {@link com.aionn.promotion.application.service.VoucherService} and is
 * triggered separately when ordering finalises the order.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VoucherApplyAdapter implements VoucherApplyPort {

    private final VoucherRepository voucherRepository;

    @Override
    @Transactional(readOnly = true)
    public Discount apply(String userId, String voucherCode, BigDecimal lineSubtotal, String currency) {
        Voucher voucher = voucherRepository.findByCode(voucherCode).orElse(null);
        if (voucher == null) {
            return new Discount(BigDecimal.ZERO, currency, false, "voucher-not-found");
        }
        if (!voucher.isUsable(Instant.now())) {
            return new Discount(BigDecimal.ZERO, currency, false, "voucher-not-usable");
        }
        BigDecimal discount = voucher.getDiscountAmount().amount();
        if (lineSubtotal != null && discount.compareTo(lineSubtotal) > 0) {
            discount = lineSubtotal;
        }
        String discountCurrency = voucher.getDiscountAmount().currency();
        return new Discount(discount, discountCurrency, true, null);
    }
}
