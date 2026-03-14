package com.ecommerce.sharedkernel.application.event;

import com.ecommerce.sharedkernel.domain.model.DomainEvent;

public interface DomainEventHandler<E extends DomainEvent> {
    void handle(E event);
}