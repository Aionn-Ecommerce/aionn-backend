package com.aionn.promotion.infrastructure.scheduling;

import com.aionn.promotion.application.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "promotion.voucher.auto-release", name = "enabled", havingValue = "true", matchIfMissing = true)
public class VoucherAutoReleaseScheduler {

    private final VoucherService voucherService;

    @Value("${promotion.voucher.auto-release.batch-size:100}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${promotion.voucher.auto-release.delay-ms:30000}")
    public void run() {
        try {
            int released = voucherService.releaseExpiredReservations(Instant.now(), batchSize);
            if (released > 0) {
                log.info("Voucher auto-release returned {} expired reservation(s) to the pool", released);
            }
        } catch (Exception ex) {
            log.error("Voucher auto-release sweep failed", ex);
        }
    }
}

