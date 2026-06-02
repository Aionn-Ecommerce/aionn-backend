package com.aionn.sharedkernel.integration.event.ordering;

import com.aionn.sharedkernel.integration.event.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when an order has been completed (delivered and confirmed).
 * 
 * <p>
 * Consumers may use this event to:
 * </p>
 * <ul>
 * <li>Request customer review</li>
 * <li>Update merchant performance metrics</li>
 * <li>Finalize accounting records</li>
 * </ul>
 */
public record OrderCompletedIntegrationEvent(
        String eventId,
        String orderId,
        Instant occurredAt) implements IntegrationEvent {

    public OrderCompletedIntegrationEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
    }
}
