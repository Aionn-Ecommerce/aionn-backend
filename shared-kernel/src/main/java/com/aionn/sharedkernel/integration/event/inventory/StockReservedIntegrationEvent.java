package com.aionn.sharedkernel.integration.event.inventory;

import com.aionn.sharedkernel.integration.event.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when stock has been successfully reserved for an order.
 * 
 * <p>
 * Consumers may use this event to:
 * </p>
 * <ul>
 * <li>Proceed with order processing</li>
 * <li>Update inventory analytics</li>
 * </ul>
 */
public record StockReservedIntegrationEvent(
        String eventId,
        String reservationId,
        String skuId,
        String warehouseId,
        String orderId,
        int quantity,
        Instant expiresAt,
        Instant occurredAt) implements IntegrationEvent {

    public StockReservedIntegrationEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
    }
}
