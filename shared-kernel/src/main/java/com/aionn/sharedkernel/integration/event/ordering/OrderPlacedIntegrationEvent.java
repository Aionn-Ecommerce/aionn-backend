package com.aionn.sharedkernel.integration.event.ordering;

import com.aionn.sharedkernel.integration.event.IntegrationEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Published when a new order has been successfully placed.
 * 
 * <p>
 * Consumers may use this event to:
 * </p>
 * <ul>
 * <li>Reserve inventory</li>
 * <li>Process payment</li>
 * <li>Send order confirmation notifications</li>
 * <li>Update analytics and reporting</li>
 * </ul>
 */
public record OrderPlacedIntegrationEvent(
        String eventId,
        String orderId,
        String userId,
        String merchantId,
        String proposalId,
        List<OrderLineItem> items,
        BigDecimal totalAmount,
        String currency,
        String addressId,
        String paymentMethodId,
        Instant occurredAt) implements IntegrationEvent {

    public OrderPlacedIntegrationEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID().toString();
        }
    }

    public record OrderLineItem(
            String skuId,
            int quantity,
            BigDecimal unitPrice,
            String warehouseId,
            String reservationId) {
    }
}
