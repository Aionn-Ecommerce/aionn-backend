package com.aionn.inventory.infrastructure.scheduling;

import com.aionn.inventory.application.dto.reservation.command.ReservationCommands;
import com.aionn.inventory.application.service.StockReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Per-row {@link Propagation#REQUIRES_NEW} worker so a single reservation's
 * release failure does not poison the whole batch transaction (audit B6).
 * Lives in a separate bean so the {@code @Transactional} proxy actually
 * applies (Spring proxies do not intercept self-invocation).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationAutoReleaseWorker {

    private final StockReservationService reservationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void releaseOne(String reservationId) {
        reservationService.release(new ReservationCommands.ReleaseReservation(reservationId, "expired"));
    }
}
