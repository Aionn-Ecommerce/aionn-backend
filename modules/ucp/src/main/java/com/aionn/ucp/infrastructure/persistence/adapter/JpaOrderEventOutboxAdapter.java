package com.aionn.ucp.infrastructure.persistence.adapter;

import com.aionn.sharedkernel.util.IdGenerator;
import com.aionn.ucp.application.port.out.OrderEventOutboxPort;
import com.aionn.ucp.domain.model.OrderEventStatus;
import com.aionn.ucp.infrastructure.persistence.entity.OrderEventOutboxEntity;
import com.aionn.ucp.infrastructure.persistence.repository.OrderEventOutboxJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JpaOrderEventOutboxAdapter implements OrderEventOutboxPort {

    private final OrderEventOutboxJpaRepository jpa;
    private final ObjectMapper objectMapper;

    @Override
    public void enqueue(String orderId, String sessionId, String webhookUrl,
            String eventType, Map<String, Object> payload) {
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialise outbox payload", ex);
        }
        OrderEventOutboxEntity entity = new OrderEventOutboxEntity();
        entity.setEventId("evt_" + IdGenerator.ulid());
        entity.setOrderId(orderId);
        entity.setSessionId(sessionId);
        entity.setWebhookUrl(webhookUrl);
        entity.setEventType(eventType);
        entity.setPayloadJson(payloadJson);
        entity.setStatus(OrderEventStatus.PENDING.name());
        entity.setAttempts(0);
        entity.setCreatedAt(Instant.now());
        jpa.save(entity);
    }

    @Override
    public List<OutboxEvent> findPending(int batchSize, int maxAttempts) {
        return jpa.findPending(maxAttempts, PageRequest.of(0, Math.max(1, batchSize))).stream()
                .map(JpaOrderEventOutboxAdapter::toEvent)
                .toList();
    }

    @Override
    public void markDelivered(String eventId, Instant when) {
        jpa.findById(eventId).ifPresent(e -> {
            e.setStatus(OrderEventStatus.DELIVERED.name());
            e.setDeliveredAt(when);
            e.setLastAttemptAt(when);
            jpa.save(e);
        });
    }

    @Override
    public void recordAttemptFailure(String eventId, String error, boolean exceededRetries, Instant when) {
        jpa.findById(eventId).ifPresent(e -> {
            e.setAttempts(e.getAttempts() + 1);
            e.setLastError(truncate(error));
            e.setLastAttemptAt(when);
            if (exceededRetries) {
                e.setStatus(OrderEventStatus.FAILED.name());
            }
            jpa.save(e);
        });
    }

    private static String truncate(String text) {
        if (text == null)
            return null;
        return text.length() > 1000 ? text.substring(0, 1000) : text;
    }

    private static OutboxEvent toEvent(OrderEventOutboxEntity e) {
        return new OutboxEvent(
                e.getEventId(),
                e.getOrderId(),
                e.getSessionId(),
                e.getWebhookUrl(),
                e.getEventType(),
                e.getPayloadJson(),
                e.getAttempts(),
                e.getCreatedAt());
    }
}
