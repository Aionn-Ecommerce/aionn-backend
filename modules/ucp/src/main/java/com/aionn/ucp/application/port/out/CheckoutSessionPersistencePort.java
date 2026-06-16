package com.aionn.ucp.application.port.out;

import com.aionn.ucp.domain.model.CheckoutSessionStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CheckoutSessionPersistencePort {

    Session save(Session session);

    Optional<Session> findById(String sessionId);

    record Session(
            String sessionId,
            String userId,
            String platformProfileUrl,
            String webhookUrl,
            CheckoutSessionStatus status,
            String currency,
            String lineItemsJson,
            String totalsJson,
            String discountsJson,
            String orderId,
            String cartId,
            String continueUrl,
            Instant createdAt,
            Instant updatedAt) {
    }

    java.util.Optional<Session> findByOrderId(String orderId);

    java.util.Optional<Session> findByCartId(String cartId);

    record LineItemSnapshot(String skuId, int quantity, long unitPriceMinor, String title) {
    }

    interface LineCodec {
        String encode(List<LineItemSnapshot> lines);

        List<LineItemSnapshot> decode(String json);
    }
}
