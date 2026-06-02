package com.aionn.sharedkernel.integration.event.catalog;

import com.aionn.sharedkernel.integration.event.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when a merchant has been suspended.
 * 
 * <p>
 * Consumers may use this event to:
 * </p>
 * <ul>
 * <li>Hide merchant products from search</li>
 * <li>Cancel pending orders</li>
 * <li>Disable inventory operations</li>
 * <li>Notify affected customers</li>
 * </ul>
 */
public record MerchantSuspendedIntegrationEvent(
        String eventId,
        String merchantId,
        String reason,
        Instant occurredAt) implements IntegrationEvent {

    public MerchantSuspendedIntegrationEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
    }
}
