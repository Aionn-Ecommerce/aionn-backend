package com.aionn.sharedkernel.application.port;

import com.aionn.sharedkernel.domain.model.DomainEvent;
import com.aionn.sharedkernel.domain.model.EventEnvelope;
import com.aionn.sharedkernel.util.IdGenerator;

import java.util.Collection;
import java.util.List;

public interface EventPublisher {

    void publish(Collection<EventEnvelope> events);

    default void publish(EventEnvelope event) {
        publish(List.of(event));
    }

    /**
     * Convenience for publishing events without an AggregateRoot.
     * Prefer using AggregateRoot.record() + pullEvents() for domain entities.
     *
     * @deprecated Migrate callers to use AggregateRoot event recording pattern
     *             instead.
     */
    @Deprecated(forRemoval = true)
    default void publishDirect(String aggregateType, String aggregateId, DomainEvent event) {
        publish(new EventEnvelope(IdGenerator.ulid(), aggregateType, aggregateId, event, event.occurredAt()));
    }

    /**
     * @deprecated Migrate callers to use AggregateRoot event recording pattern
     *             instead.
     */
    @Deprecated(forRemoval = true)
    default void publishDirect(String aggregateType, String aggregateId, List<? extends DomainEvent> events) {
        List<EventEnvelope> envelopes = events.stream()
                .map(e -> new EventEnvelope(IdGenerator.ulid(), aggregateType, aggregateId, e, e.occurredAt()))
                .toList();
        publish(envelopes);
    }
}
