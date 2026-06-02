package com.aionn.sharedkernel.integration.publisher;

import com.aionn.sharedkernel.integration.event.IntegrationEvent;

import java.util.Collection;

/**
 * Publisher for integration events that cross module boundaries.
 * 
 * <p>
 * This abstraction allows for different implementations:
 * </p>
 * <ul>
 * <li>Spring ApplicationEventPublisher (in-memory, for monolith)</li>
 * <li>Kafka Producer (for microservices)</li>
 * <li>RabbitMQ Publisher (for microservices)</li>
 * <li>AWS SNS/SQS (for cloud-native)</li>
 * </ul>
 * 
 * <p>
 * The publisher is responsible for:
 * </p>
 * <ul>
 * <li>Serializing events</li>
 * <li>Routing events to appropriate channels/topics</li>
 * <li>Ensuring at-least-once delivery semantics</li>
 * <li>Handling transactional boundaries (outbox pattern if needed)</li>
 * </ul>
 */
public interface IntegrationEventPublisher {

    /**
     * Publish a single integration event.
     * 
     * @param event the event to publish
     */
    void publish(IntegrationEvent event);

    /**
     * Publish multiple integration events in batch.
     * 
     * @param events the events to publish
     */
    void publishAll(Collection<IntegrationEvent> events);
}
