package com.aionn.sharedkernel.integration.event.ordering;

import com.aionn.sharedkernel.integration.event.IntegrationEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when an order has been cancelled by user or system.
 * 
 * <p>
 * Consumers may use this event to:
 * </p>
 * <ul>
 * <li>Release inventory reservations</li>
 * <li>Refund payment</li>
 * <li>Release vouchers</li>
 * <li>Cancel shipment</li>
 * <li>Send cancellation notification</li>
 * </ul>
 */
public record OrderCancelledIntegrationEvent(
        String eventId,
        String orderId,
        String reasonCode,
        String reason,
        CancellationType cancellationType,
        Instant occurredAt) implements IntegrationEvent {

    public OrderCancelledIntegrationEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
    }

    public enum CancellationType {
        USER_CANCELLED,
        AUTO_CANCELLED,
        MERCHANT_REJECTED
    }
}
