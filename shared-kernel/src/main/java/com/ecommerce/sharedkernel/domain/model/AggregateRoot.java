package com.ecommerce.sharedkernel.domain.model;

import com.ecommerce.sharedkernel.domain.id.BaseId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AggregateRoot<ID extends BaseId> extends Entity<ID> {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected AggregateRoot(ID id) {
        super(id);
    }

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(events);
    }

    public List<DomainEvent> peekDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public boolean hasUnpublishedEvents() {
        return !domainEvents.isEmpty();
    }
}
