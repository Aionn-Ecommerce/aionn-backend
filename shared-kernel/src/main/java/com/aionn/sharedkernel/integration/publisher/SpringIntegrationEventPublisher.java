package com.aionn.sharedkernel.integration.publisher;

import com.aionn.sharedkernel.integration.event.IntegrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Spring-based implementation of IntegrationEventPublisher.
 * 
 * <p>
 * Uses Spring's ApplicationEventPublisher for in-memory event distribution
 * within the modular monolith. Events are published synchronously within the
 * same transaction boundary.
 * </p>
 * 
 * <p>
 * This implementation is suitable for:
 * </p>
 * <ul>
 * <li>Modular monolith architecture</li>
 * <li>Development and testing environments</li>
 * <li>Low-latency event processing requirements</li>
 * </ul>
 * 
 * <p>
 * For production microservices, replace with Kafka/RabbitMQ implementation.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringIntegrationEventPublisher implements IntegrationEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(IntegrationEvent event) {
        log.debug("Publishing integration event: {} [eventId={}, occurredAt={}]",
                event.eventType(), event.eventId(), event.occurredAt());

        applicationEventPublisher.publishEvent(event);

        log.trace("Integration event published successfully: {}", event.eventType());
    }

    @Override
    public void publishAll(Collection<IntegrationEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        log.debug("Publishing {} integration events in batch", events.size());

        for (IntegrationEvent event : events) {
            publish(event);
        }

        log.trace("Batch of {} integration events published successfully", events.size());
    }
}
