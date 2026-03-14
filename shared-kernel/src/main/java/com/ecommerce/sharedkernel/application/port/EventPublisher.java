package com.ecommerce.sharedkernel.application.port;

import java.util.List;

import com.ecommerce.sharedkernel.application.event.IntegrationEvent;
import com.ecommerce.sharedkernel.domain.model.DomainEvent;

public interface EventPublisher {

    void publish(DomainEvent event);

    default void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }

    void publishIntegration(IntegrationEvent event);
}
