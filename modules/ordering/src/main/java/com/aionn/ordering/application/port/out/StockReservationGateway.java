package com.aionn.ordering.application.port.out;

import java.math.BigDecimal;
import java.util.List;

/**
 * Outbound port to the Inventory bounded context. The default implementation
 * calls inventory's reservation service in-process; later it can be swapped
 * for a remote/HTTP client without touching this layer.
 */
public interface StockReservationGateway {

    /**
     * Reserve every line in {@code lines}. Returns one {@link Reservation}
     * per requested line. If any reservation fails, all previously created
     * reservations are released by the gateway (best-effort) and a
     * {@link ReservationException} is thrown so the orchestrator can
     * cancel the order.
     */
    List<Reservation> reserveAll(String orderId, List<ReservationLine> lines, int ttlSeconds);

    /** Commit (decrement physical stock) the given reservation. */
    void commit(String reservationId);

    /** Release a reservation (qty returns to available pool). */
    void release(String reservationId, String reason);

    record ReservationLine(String skuId, String warehouseId, int qty, BigDecimal unitPrice, String currency) {
    }

    record Reservation(String reservationId, String skuId, String warehouseId, int qty, BigDecimal unitPrice,
            String currency) {
    }

    class ReservationException extends RuntimeException {
        private final String skuId;

        public ReservationException(String skuId, String message) {
            super(message);
            this.skuId = skuId;
        }

        public String getSkuId() {
            return skuId;
        }
    }
}

