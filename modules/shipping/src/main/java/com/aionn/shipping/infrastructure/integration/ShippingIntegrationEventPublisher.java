package com.aionn.shipping.infrastructure.integration;

import com.aionn.shipping.application.port.out.integration.ShippingIntegrationEventPublisherPort;
import com.aionn.sharedkernel.integration.event.shipping.ShipmentDeliveredIntegrationEvent;
import com.aionn.sharedkernel.integration.event.shipping.ShipmentDeliveryFailedIntegrationEvent;
import com.aionn.sharedkernel.integration.event.shipping.ShipmentDispatchedIntegrationEvent;
import com.aionn.sharedkernel.integration.publisher.IntegrationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ShippingIntegrationEventPublisher implements ShippingIntegrationEventPublisherPort {

    private final IntegrationEventPublisher integrationEventPublisher;

    @Override
    public void publishDispatched(String shipmentId, String orderId, String trackingCode) {
        integrationEventPublisher.publish(new ShipmentDispatchedIntegrationEvent(
                null, shipmentId, orderId, trackingCode, Instant.now()));
    }

    @Override
    public void publishDelivered(String shipmentId, String orderId, String signatureUrl, Instant deliveredAt) {
        integrationEventPublisher.publish(new ShipmentDeliveredIntegrationEvent(
                null, shipmentId, orderId, signatureUrl, deliveredAt, Instant.now()));
    }

    @Override
    public void publishDeliveryFailed(String shipmentId, String orderId, String reason, int attemptCount) {
        integrationEventPublisher.publish(new ShipmentDeliveryFailedIntegrationEvent(
                null, shipmentId, orderId, reason, attemptCount, Instant.now()));
    }
}
