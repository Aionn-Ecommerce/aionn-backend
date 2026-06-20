package com.aionn.ordering.infrastructure.gateway;

import com.aionn.ordering.application.port.out.StockReservationGateway;
import com.aionn.sharedkernel.integration.port.inventory.InventoryStockReservationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter that bridges ordering's local outbound port to the cross-service
 * shared-kernel port. Ordering keeps its own port surface so it can swap the
 * transport (in-process call vs HTTP/gRPC) without changing application code.
 */
@Component
@RequiredArgsConstructor
public class InventoryStockReservationGateway implements StockReservationGateway {

    private final InventoryStockReservationPort inventoryStockReservation;

    @Override
    public List<Reservation> reserveAll(String orderId, List<ReservationLine> lines, int ttlSeconds) {
        try {
            List<InventoryStockReservationPort.ReservationLine> mapped = lines.stream()
                    .map(l -> new InventoryStockReservationPort.ReservationLine(
                            l.skuId(), l.warehouseId(), l.qty(), l.unitPrice(), l.currency()))
                    .toList();
            List<InventoryStockReservationPort.Reservation> result =
                    inventoryStockReservation.reserveAll(orderId, mapped, ttlSeconds);
            return result.stream()
                    .map(r -> new Reservation(r.reservationId(), r.skuId(), r.warehouseId(),
                            r.qty(), r.unitPrice(), r.currency()))
                    .toList();
        } catch (InventoryStockReservationPort.ReservationException ex) {
            throw new ReservationException(ex.getSkuId(), ex.getMessage());
        }
    }

    @Override
    public void commit(String reservationId) {
        inventoryStockReservation.commit(reservationId);
    }

    @Override
    public void release(String reservationId, String reason) {
        inventoryStockReservation.release(reservationId, reason);
    }
}
