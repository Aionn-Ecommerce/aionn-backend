package com.ecommerce.sharedkernel.domain.event;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredOn();

    String eventType();
}