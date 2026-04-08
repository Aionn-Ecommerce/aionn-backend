package com.ecommerce.sharedkernel.testing;

import com.ecommerce.sharedkernel.application.event.IntegrationEvent;
import com.ecommerce.sharedkernel.application.port.EventPublisher;
import com.ecommerce.sharedkernel.domain.model.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FakeEventPublisher implements EventPublisher {

    private final List<DomainEvent> domainEvents = new ArrayList<>();
    private final List<IntegrationEvent> integrationEvents = new ArrayList<>();

    @Override
    public void publish(DomainEvent event) {
        domainEvents.add(event);
    }

    @Override
    public void publishIntegration(IntegrationEvent event) {
        integrationEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public List<IntegrationEvent> getIntegrationEvents() {
        return Collections.unmodifiableList(integrationEvents);
    }

    public <T extends DomainEvent> List<T> domainEventsOf(Class<T> type) {
        return domainEvents.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(Collectors.toList());
    }

    public <T extends DomainEvent> T firstOf(Class<T> type) {
        return domainEventsOf(type).stream()
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No domain event of type " + type.getSimpleName() + " was published"));
    }

    public boolean hasPublished(Class<? extends DomainEvent> type) {
        return domainEvents.stream().anyMatch(type::isInstance);
    }

    public int domainEventCount() {
        return domainEvents.size();
    }

    public boolean isEmpty() {
        return domainEvents.isEmpty() && integrationEvents.isEmpty();
    }

    public void assertPublished(Class<? extends DomainEvent> type) {
        if (!hasPublished(type)) {
            throw new AssertionError(
                    "Expected domain event of type [%s] to be published, but it wasn't. Published events: %s"
                            .formatted(type.getSimpleName(), domainEventNames()));
        }
    }

    public void assertNotPublished(Class<? extends DomainEvent> type) {
        if (hasPublished(type)) {
            throw new AssertionError("Expected no domain event of type [%s], but it was published."
                    .formatted(type.getSimpleName()));
        }
    }

    public void assertPublishedCount(int expectedCount) {
        if (domainEvents.size() != expectedCount) {
            throw new AssertionError("Expected %d domain event(s) to be published, but got %d. Events: %s"
                    .formatted(expectedCount, domainEvents.size(), domainEventNames()));
        }
    }

    public void assertNoEventsPublished() {
        if (!domainEvents.isEmpty()) {
            throw new AssertionError("Expected no domain events, but %d were published: %s"
                    .formatted(domainEvents.size(), domainEventNames()));
        }
    }

    public void reset() {
        domainEvents.clear();
        integrationEvents.clear();
    }

    private List<String> domainEventNames() {
        return domainEvents.stream()
                .map(e -> e.getClass().getSimpleName())
                .collect(Collectors.toList());
    }
}
