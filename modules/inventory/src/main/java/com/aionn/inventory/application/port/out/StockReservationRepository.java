package com.aionn.inventory.application.port.out;

import com.aionn.inventory.domain.model.StockReservation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface StockReservationRepository {

    StockReservation save(StockReservation reservation);

    Optional<StockReservation> findById(String reservationId);

    /**
     * Find every RESERVED row whose {@code expiresAt} is at or before
     * {@code now}. Used by the periodic auto-release sweep (UC4.20).
     */
    List<StockReservation> findExpired(Instant now, int limit);
}

