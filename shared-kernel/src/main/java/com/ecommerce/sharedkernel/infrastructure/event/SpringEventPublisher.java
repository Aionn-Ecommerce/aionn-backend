package com.ecommerce.sharedkernel.infrastructure.event;

import com.ecommerce.sharedkernel.application.event.IntegrationEvent;
import com.ecommerce.sharedkernel.application.port.EventPublisher;
import com.ecommerce.sharedkernel.domain.model.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SpringEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        log.debug("Publishing domain event: {} [aggregate={}, id={}]",
                event.getEventName(), event.getAggregateType(), event.getAggregateId());
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishIntegration(IntegrationEvent event) {
        log.info("Publishing integration event: {} [source={}, id={}]",
                event.getEventType(), event.getSourceModule(), event.getEventId());
        applicationEventPublisher.publishEvent(event);
    }
}
