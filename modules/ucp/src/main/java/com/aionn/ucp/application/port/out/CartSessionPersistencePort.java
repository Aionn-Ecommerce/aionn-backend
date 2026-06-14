package com.aionn.ucp.application.port.out;

import java.time.Instant;
import java.util.Optional;

public interface CartSessionPersistencePort {

    record CartSession(
            String cartId,
            String userId,
            String currency,
            String lineItemsJson, // JSON array of line item snapshots
            String totalsJson, // Simple JSON with subtotal and total
            String continueUrl,
            Instant createdAt,
            Instant updatedAt) {
    }

    CartSession save(CartSession session);

    Optional<CartSession> findById(String cartId);

    Optional<CartSession> findByUserId(String userId);

    void deleteById(String cartId);
}
