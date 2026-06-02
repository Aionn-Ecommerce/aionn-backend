package com.aionn.sharedkernel.integration.event.ordering;

import com.aionn.sharedkernel.integration.event.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when an order has been shipped.
 * 
 * <p>
 * Consumers may use this event to:
 * </p>
 * <ul>
 * <li>Send shipment tracking notification</li>
 * <li>Update order status in analytics</li>
 * </ul>
 */
public record OrderShippedIntegrationEvent(
        String eventId,
        String orderId,
        String shipmentId,
        Instant occurredAt) implements IntegrationEvent {

    public OrderShippedIntegrationEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
    }
}
