package com.aionn.inventory.infrastructure.scheduling;

import com.aionn.inventory.application.service.StockReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * UC4.20 - sweep expired RESERVED reservations and release them. Runs at a
 * configurable fixed delay; can be turned off with
 * {@code inventory.reservation.auto-release.enabled=false}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "inventory.reservation.auto-release", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ReservationAutoReleaseScheduler {

    private final StockReservationService reservationService;

    @Value("${inventory.reservation.auto-release.batch-size:100}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${inventory.reservation.auto-release.delay-ms:30000}")
    public void releaseExpired() {
        try {
            int released = reservationService.autoReleaseExpired(Instant.now(), batchSize);
            if (released > 0) {
                log.info("Auto-released {} expired reservation(s)", released);
            }
        } catch (Exception ex) {
            log.error("Failed to auto-release expired reservations", ex);
        }
    }
}

