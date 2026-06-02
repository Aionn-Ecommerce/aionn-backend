package com.aionn.sharedkernel.integration.mapper;

/**
 * Base interface for mappers that convert domain events to integration events.
 * 
 * <p>
 * Each module should implement mappers to translate their internal domain
 * events
 * into integration events that can be consumed by other modules.
 * </p>
 * 
 * <p>
 * Mapping responsibilities:
 * </p>
 * <ul>
 * <li>Extract only necessary data (avoid leaking internal domain details)</li>
 * <li>Generate eventId if not present</li>
 * <li>Preserve occurredAt timestamp</li>
 * <li>Convert domain objects to simple DTOs</li>
 * </ul>
 * 
 * @param <D> Domain event type
 * @param <I> Integration event type
 */
public interface IntegrationEventMapper<D, I> {

    /**
     * Map a domain event to an integration event.
     * 
     * @param domainEvent the domain event from the module
     * @return the integration event to be published
     */
    I toIntegrationEvent(D domainEvent);
}
