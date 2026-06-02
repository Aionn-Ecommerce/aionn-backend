package com.aionn.sharedkernel.integration.event;

import java.time.Instant;

/**
 * Marker interface for all integration events that cross module boundaries.
 * 
 * <p>
 * Integration events are used for asynchronous communication between modules
 * in the modular monolith architecture. They represent facts that have occurred
 * in one module and may be of interest to other modules.
 * </p>
 * 
 * <p>
 * Key characteristics:
 * </p>
 * <ul>
 * <li>Immutable - events represent facts that have already happened</li>
 * <li>Self-contained - contain all data needed by consumers</li>
 * <li>Versioned - support evolution without breaking consumers</li>
 * <li>Broker-ready - designed to be easily migrated to message brokers (Kafka,
 * RabbitMQ)</li>
 * </ul>
 * 
 * <p>
 * Implementation notes:
 * </p>
 * <ul>
 * <li>Use Java records for immutability</li>
 * <li>Include occurredAt timestamp for event ordering</li>
 * <li>Include eventId for idempotency and tracing</li>
 * <li>Avoid domain objects - use primitive types and simple DTOs</li>
 * </ul>
 */
public interface IntegrationEvent {

    /**
     * Unique identifier for this event instance.
     * Used for idempotency checks and distributed tracing.
     */
    String eventId();

    /**
     * Timestamp when the event occurred.
     * Used for event ordering and temporal queries.
     */
    Instant occurredAt();

    /**
     * Type identifier for this event.
     * Used for routing and deserialization.
     */
    default String eventType() {
        return this.getClass().getSimpleName();
    }
}
