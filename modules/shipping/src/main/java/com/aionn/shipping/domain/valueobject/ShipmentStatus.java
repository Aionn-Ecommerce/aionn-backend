package com.aionn.shipping.domain.valueobject;

/**
 * Shipment lifecycle.
 *
 * <pre>
 *   REQUESTED â†’ REGISTERED â†’ PICKED_UP â†’ IN_TRANSIT
 *                              â†“             â†“
 *                              CANCELLED     OUT_FOR_DELIVERY
 *                                              â†“        â†“
 *                                       DELIVERED   DELIVERY_FAILED
 *                                                       â†“
 *                                       (auto retry)  RETURNED
 * </pre>
 */
public enum ShipmentStatus {
    REQUESTED,
    REGISTERED,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    DELIVERY_FAILED,
    RETURNED,
    CANCELLED;

    public boolean canTransitionTo(ShipmentStatus next) {
        return switch (this) {
            case REQUESTED -> next == REGISTERED || next == CANCELLED;
            case REGISTERED -> next == PICKED_UP || next == CANCELLED;
            case PICKED_UP -> next == IN_TRANSIT || next == OUT_FOR_DELIVERY;
            case IN_TRANSIT -> next == OUT_FOR_DELIVERY || next == IN_TRANSIT;
            case OUT_FOR_DELIVERY -> next == DELIVERED || next == DELIVERY_FAILED;
            case DELIVERY_FAILED -> next == OUT_FOR_DELIVERY || next == RETURNED;
            case DELIVERED, RETURNED, CANCELLED -> false;
        };
    }

    public boolean isPickedUp() {
        return this != REQUESTED && this != REGISTERED && this != CANCELLED;
    }
}

