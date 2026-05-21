package com.aionn.ordering.infrastructure.scheduling;

import com.aionn.ordering.application.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * UC5.10 - cancels orders that have been pending payment longer than the
 * configured timeout. Toggle via
 * {@code ordering.auto-cancel.enabled=false} in dev.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ordering.auto-cancel", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OrderAutoCancelScheduler {

    private final OrderService orderService;

    @Value("${ordering.auto-cancel.timeout-minutes:15}")
    private int timeoutMinutes;

    @Value("${ordering.auto-cancel.batch-size:100}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${ordering.auto-cancel.delay-ms:60000}")
    public void run() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(timeoutMinutes));
        try {
            int cancelled = orderService.autoCancelExpired(cutoff, batchSize);
            if (cancelled > 0) {
                log.info("Auto-cancelled {} order(s) older than {} minutes", cancelled, timeoutMinutes);
            }
        } catch (Exception ex) {
            log.error("Auto-cancel sweep failed", ex);
        }
    }
}

