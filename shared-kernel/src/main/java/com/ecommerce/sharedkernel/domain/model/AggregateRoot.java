package com.ecommerce.sharedkernel.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.ecommerce.sharedkernel.domain.event.DomainEvent;
import com.ecommerce.sharedkernel.domain.event.DomainEventPublisher;

public abstract class AggregateRoot<ID> extends BaseEntity<ID> {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected AggregateRoot(ID id) {
        super(id);
    }

    protected void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public void clearEvents() {
        this.domainEvents.clear();
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void publishEvents(DomainEventPublisher publisher) {
        publisher.publishAll(domainEvents);
        clearEvents();
    }
}