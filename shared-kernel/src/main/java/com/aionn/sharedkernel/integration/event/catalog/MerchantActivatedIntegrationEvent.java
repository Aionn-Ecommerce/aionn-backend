package com.aionn.sharedkernel.integration.event.catalog;

import com.aionn.sharedkernel.integration.event.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when a merchant has been activated or reactivated.
 * 
 * <p>
 * Consumers may use this event to:
 * </p>
 * <ul>
 * <li>Reindex merchant products in search</li>
 * <li>Enable inventory operations</li>
 * <li>Resume order processing</li>
 * </ul>
 */
public record MerchantActivatedIntegrationEvent(
        String eventId,
        String merchantId,
        String adminId,
        Instant occurredAt) implements IntegrationEvent {

    public MerchantActivatedIntegrationEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
    }
}
