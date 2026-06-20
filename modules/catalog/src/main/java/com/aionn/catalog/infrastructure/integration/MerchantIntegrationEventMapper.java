package com.aionn.catalog.infrastructure.integration;

import com.aionn.catalog.domain.event.MerchantEvents;
import com.aionn.catalog.domain.event.ProductEvents;
import com.aionn.sharedkernel.integration.event.catalog.*;
import org.springframework.stereotype.Component;

/**
 * Maps Catalog domain events to integration events.
 * 
 * <p>
 * This mapper extracts only the necessary data from domain events
 * and converts them into integration events that can be consumed by other
 * modules.
 * </p>
 */
@Component
public class MerchantIntegrationEventMapper {

    public MerchantSuspendedIntegrationEvent toIntegrationEvent(MerchantEvents.MerchantSuspended domainEvent) {
        return new MerchantSuspendedIntegrationEvent(
                null, // eventId will be auto-generated
                domainEvent.merchantId(),
                domainEvent.reason(),
                domainEvent.occurredAt());
    }

    public MerchantClosedIntegrationEvent toIntegrationEvent(MerchantEvents.MerchantClosed domainEvent) {
        return new MerchantClosedIntegrationEvent(
                null,
                domainEvent.merchantId(),
                domainEvent.reason(),
                domainEvent.occurredAt());
    }

    public MerchantActivatedIntegrationEvent toIntegrationEvent(MerchantEvents.MerchantActivated domainEvent) {
        return new MerchantActivatedIntegrationEvent(
                null,
                domainEvent.merchantId(),
                domainEvent.adminId(),
                domainEvent.occurredAt());
    }

    public ProductEmergencyTakedownIntegrationEvent toIntegrationEvent(
            ProductEvents.ProductEmergencyTakedown domainEvent) {
        return new ProductEmergencyTakedownIntegrationEvent(
                null,
                domainEvent.productId(),
                domainEvent.adminId(),
                domainEvent.reason(),
                domainEvent.occurredAt());
    }
}
