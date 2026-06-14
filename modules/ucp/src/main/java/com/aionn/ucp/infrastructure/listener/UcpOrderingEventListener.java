package com.aionn.ucp.infrastructure.listener;

import com.aionn.sharedkernel.integration.event.ordering.OrderApprovedIntegrationEvent;
import com.aionn.sharedkernel.integration.event.ordering.OrderCancelledIntegrationEvent;
import com.aionn.sharedkernel.integration.event.ordering.OrderCompletedIntegrationEvent;
import com.aionn.sharedkernel.integration.event.ordering.OrderShippedIntegrationEvent;
import com.aionn.ucp.application.port.out.CheckoutSessionPersistencePort;
import com.aionn.ucp.application.port.out.OrderEventOutboxPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UcpOrderingEventListener {

    private final CheckoutSessionPersistencePort sessionRepository;
    private final OrderEventOutboxPort outbox;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOrderApproved(OrderApprovedIntegrationEvent event) {
        enqueueIfUcpOrder(event.orderId(), "order.confirmed", buildPayload(event.orderId(), Map.of(
                "payment_id", event.paymentId(),
                "occurred_at", Instant.now().toString())));
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOrderShipped(OrderShippedIntegrationEvent event) {
        enqueueIfUcpOrder(event.orderId(), "order.shipped", buildPayload(event.orderId(), Map.of(
                "shipment_id", event.shipmentId(),
                "occurred_at", Instant.now().toString())));
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOrderCompleted(OrderCompletedIntegrationEvent event) {
        enqueueIfUcpOrder(event.orderId(), "order.delivered", buildPayload(event.orderId(), Map.of(
                "occurred_at", Instant.now().toString())));
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOrderCancelled(OrderCancelledIntegrationEvent event) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reason_code", event.reasonCode());
        data.put("reason", event.reason());
        data.put("kind", event.cancellationType() == null ? null : event.cancellationType().name());
        data.put("occurred_at", Instant.now().toString());
        enqueueIfUcpOrder(event.orderId(), "order.cancelled", buildPayload(event.orderId(), data));
    }

    private void enqueueIfUcpOrder(String orderId, String eventType, Map<String, Object> payload) {
        Optional<CheckoutSessionPersistencePort.Session> sess = sessionRepository.findByOrderId(orderId);
        if (sess.isEmpty())
            return;
        String webhookUrl = sess.get().webhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank())
            return;
        try {
            outbox.enqueue(orderId, sess.get().sessionId(), webhookUrl, eventType, payload);
            log.info("UCP outbox enqueued: order={} type={} url={}", orderId, eventType, webhookUrl);
        } catch (Exception ex) {
            log.warn("Failed to enqueue UCP outbox event order={} type={}: {}", orderId, eventType, ex.getMessage());
        }
    }

    private static Map<String, Object> buildPayload(String orderId, Map<String, Object> data) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("order_id", orderId);
        payload.putAll(data);
        return payload;
    }
}
