package com.aionn.ucp.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface OrderEventOutboxPort {

        void enqueue(String orderId, String sessionId, String webhookUrl,
                        String eventType, Map<String, Object> payload);

        List<OutboxEvent> findPending(int batchSize, int maxAttempts);

        void markDelivered(String eventId, Instant when);

        void recordAttemptFailure(String eventId, String error, boolean exceededRetries, Instant when);

        record OutboxEvent(
                        String eventId,
                        String orderId,
                        String sessionId,
                        String webhookUrl,
                        String eventType,
                        String payloadJson,
                        int attempts,
                        Instant createdAt) {
        }
}
