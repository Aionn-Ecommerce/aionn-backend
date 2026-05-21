package com.aionn.ordering.infrastructure.gateway;

import com.aionn.inventory.application.dto.reservation.command.ReservationCommands;
import com.aionn.inventory.application.dto.reservation.result.ReservationResult;
import com.aionn.inventory.application.service.StockReservationService;
import com.aionn.ordering.application.port.out.StockReservationGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * In-process bridge to the Inventory module. When Inventory is split out into
 * its own service we replace this with a remote stub.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryStockReservationGateway implements StockReservationGateway {

    private final StockReservationService reservationService;

    @Override
    public List<Reservation> reserveAll(String orderId, List<ReservationLine> lines, int ttlSeconds) {
        List<Reservation> created = new ArrayList<>();
        try {
            for (ReservationLine line : lines) {
                ReservationResult result = reservationService.reserve(new ReservationCommands.ReserveStock(
                        line.skuId(), line.warehouseId(), orderId, line.qty(), ttlSeconds));
                if (!"RESERVED".equals(result.status())) {
                    throw new ReservationException(line.skuId(), "Reservation result: " + result.status());
                }
                created.add(new Reservation(result.reservationId(), line.skuId(), line.warehouseId(),
                        line.qty(), line.unitPrice(), line.currency()));
            }
            return created;
        } catch (RuntimeException ex) {
            // Compensate already-created reservations on failure so we never
            // leave dangling holds.
            for (Reservation r : created) {
                try {
                    reservationService.release(new ReservationCommands.ReleaseReservation(
                            r.reservationId(), "compensation:" + ex.getMessage()));
                } catch (Exception releaseEx) {
                    log.warn("Failed to release reservation {} during compensation: {}",
                            r.reservationId(), releaseEx.getMessage());
                }
            }
            if (ex instanceof ReservationException re) {
                throw re;
            }
            throw new ReservationException(null, ex.getMessage());
        }
    }

    @Override
    public void commit(String reservationId) {
        reservationService.commit(new ReservationCommands.CommitReservation(reservationId));
    }

    @Override
    public void release(String reservationId, String reason) {
        reservationService.release(new ReservationCommands.ReleaseReservation(reservationId,
                reason == null ? "released" : reason));
    }
}

