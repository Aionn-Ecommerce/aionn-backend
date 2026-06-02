package com.aionn.sharedkernel.integration.event.catalog;

import com.aionn.sharedkernel.integration.event.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when a merchant has been permanently closed.
 * 
 * <p>
 * Consumers may use this event to:
 * </p>
 * <ul>
 * <li>Remove merchant products from search</li>
 * <li>Cancel all pending orders</li>
 * <li>Archive inventory data</li>
 * <li>Finalize accounting and settlements</li>
 * </ul>
 */
public record MerchantClosedIntegrationEvent(
        String eventId,
        String merchantId,
        String reason,
        Instant occurredAt) implements IntegrationEvent {

    public MerchantClosedIntegrationEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
    }
}
