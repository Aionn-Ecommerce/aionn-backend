package com.aionn.sharedkernel.integration.event.ordering;

import com.aionn.sharedkernel.integration.event.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when an order has been approved (payment successful).
 * 
 * <p>
 * Consumers may use this event to:
 * </p>
 * <ul>
 * <li>Commit inventory reservations</li>
 * <li>Notify merchant to prepare order</li>
 * <li>Send payment confirmation to customer</li>
 * </ul>
 */
public record OrderApprovedIntegrationEvent(
        String eventId,
        String orderId,
        String paymentId,
        Instant occurredAt) implements IntegrationEvent {

    public OrderApprovedIntegrationEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
    }
}
