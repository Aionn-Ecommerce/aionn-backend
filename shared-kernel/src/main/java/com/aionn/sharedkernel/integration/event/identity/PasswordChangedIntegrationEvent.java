package com.aionn.sharedkernel.integration.event.identity;

import com.aionn.sharedkernel.integration.event.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when a user's password has been changed (self-service or via
 * reset).
 */
public record PasswordChangedIntegrationEvent(
        String eventId,
        String userId,
        String channelHint,
        Instant occurredAt) implements IntegrationEvent {

    public PasswordChangedIntegrationEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
    }
}
