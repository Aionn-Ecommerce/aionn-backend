package com.aionn.inventory.application.port.out;

/**
 * Outbound notification for the Ordering bounded context. Wired through the
 * project's 2-impl pattern; remote impl is a stub until Ordering exposes a
 * receive endpoint.
 */
public interface OutboundOrderNotifier {

    void notifyOutbound(String orderId, String skuId, String warehouseId, int qty);

    void notifyReservationFailed(String orderId, String skuId, String warehouseId, int qty, String reason);
}

