package com.aionn.sharedkernel.integration.event.inventory;

import com.aionn.sharedkernel.integration.event.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when reserved stock has been released (order cancelled, payment
 * failed, etc.).
 * 
 * <p>
 * Consumers may use this event to:
 * </p>
 * <ul>
 * <li>Update inventory analytics</li>
 * <li>Trigger stock reallocation</li>
 * </ul>
 */
public record StockReleasedIntegrationEvent(
        String eventId,
        String reservationId,
        String skuId,
        String warehouseId,
        String orderId,
        int quantity,
        String reason,
        Instant occurredAt) implements IntegrationEvent {

    public StockReleasedIntegrationEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
    }
}
