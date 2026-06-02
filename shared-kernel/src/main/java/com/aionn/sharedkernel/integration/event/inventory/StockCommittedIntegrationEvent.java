package com.aionn.sharedkernel.integration.event.inventory;

import com.aionn.sharedkernel.integration.event.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when reserved stock has been committed (payment successful, order
 * confirmed).
 * 
 * <p>
 * Consumers may use this event to:
 * </p>
 * <ul>
 * <li>Update inventory analytics</li>
 * <li>Trigger warehouse picking process</li>
 * </ul>
 */
public record StockCommittedIntegrationEvent(
        String eventId,
        String reservationId,
        String skuId,
        String warehouseId,
        String orderId,
        int quantity,
        Instant occurredAt) implements IntegrationEvent {

    public StockCommittedIntegrationEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
    }
}
