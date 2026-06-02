package com.aionn.sharedkernel.integration.event.identity;

import com.aionn.sharedkernel.integration.event.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when a user's primary phone has been changed.
 */
public record PhoneChangedIntegrationEvent(
        String eventId,
        String userId,
        String oldPhone,
        String newPhone,
        Instant occurredAt) implements IntegrationEvent {

    public PhoneChangedIntegrationEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
    }
}
