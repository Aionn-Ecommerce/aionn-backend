package com.ecommerce.sharedkernel.application.event;

import java.time.Instant;
import java.util.UUID;

public abstract class IntegrationEvent {

    private final String eventId;
    private final Instant occurredOn;
    private final String sourceModule;

    protected IntegrationEvent(String sourceModule) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = Instant.now();
        this.sourceModule = sourceModule;
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }

    public String getSourceModule() {
        return sourceModule;
    }

    public String getEventType() {
        return this.getClass().getSimpleName();
    }
}
