package com.ecommerce.sharedkernel.domain.model;

import java.time.Instant;
import java.util.UUID;

public abstract class DomainEvent {

    private final String eventId;
    private final Instant occurredOn;
    private final String aggregateId;
    private final String aggregateType;

    protected DomainEvent(String aggregateId, String aggregateType) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = Instant.now();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getEventName() {
        return this.getClass().getName();
    }
}
