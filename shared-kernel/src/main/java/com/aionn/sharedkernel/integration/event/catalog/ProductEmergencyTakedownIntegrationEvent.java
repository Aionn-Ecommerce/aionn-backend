package com.aionn.sharedkernel.integration.event.catalog;

import com.aionn.sharedkernel.integration.event.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when a product has been taken down in an emergency (safety, legal,
 * etc.).
 * 
 * <p>
 * Consumers may use this event to:
 * </p>
 * <ul>
 * <li>Cancel all pending orders for this product</li>
 * <li>Remove from search index immediately</li>
 * <li>Notify affected customers</li>
 * </ul>
 */
public record ProductEmergencyTakedownIntegrationEvent(
        String eventId,
        String productId,
        String adminId,
        String reason,
        Instant occurredAt) implements IntegrationEvent {

    public ProductEmergencyTakedownIntegrationEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
    }
}
