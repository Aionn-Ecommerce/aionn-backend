package com.aionn.inventory.application.dto.reservation.command;

import com.aionn.sharedkernel.application.command.Command;

public final class ReservationCommands {

    private ReservationCommands() {
    }

    public record ReserveStock(
            String skuId,
            String warehouseId,
            String orderId,
            int qty,
            int ttlSeconds) implements Command {
    }

    public record CommitReservation(String reservationId) implements Command {
    }

    public record ReleaseReservation(String reservationId, String reason) implements Command {
    }
}
