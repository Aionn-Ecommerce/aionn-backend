package com.ecommerce.sharedkernel.domain.event;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class DomainEventPublisher {
    private static final DomainEventPublisher INSTANCE = new DomainEventPublisher();

    private final List<Consumer<DomainEvent>> subscribers = new CopyOnWriteArrayList<>();

    private DomainEventPublisher() {
    }

    public static DomainEventPublisher getInstance() {
        return INSTANCE;
    }

    public AutoCloseable subscribe(Consumer<DomainEvent> subscriber) {
        Objects.requireNonNull(subscriber, "subscriber must not be null");
        subscribers.add(subscriber);
        return () -> subscribers.remove(subscriber);
    }

    public void publish(DomainEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        for (Consumer<DomainEvent> subscriber : subscribers) {
            subscriber.accept(event);
        }
    }

    public void publishAll(List<? extends DomainEvent> events) {
        Objects.requireNonNull(events, "events must not be null");
        for (DomainEvent event : events) {
            publish(event);
        }
    }
}
