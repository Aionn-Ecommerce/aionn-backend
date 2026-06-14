package com.aionn.ucp.infrastructure.webhook;

import com.aionn.ucp.application.port.out.OrderEventOutboxPort;
import com.aionn.ucp.application.port.out.OrderEventOutboxPort.OutboxEvent;
import com.aionn.ucp.infrastructure.config.UcpProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderWebhookDeliveryWorker {

    private final OrderEventOutboxPort outbox;
    private final OrderWebhookClient webhookClient;
    private final UcpProperties properties;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deliverOne(OutboxEvent event) {
        Instant now = Instant.now();
        try {
            int status = webhookClient.post(event.webhookUrl(), event.payloadJson());
            if (status >= 200 && status < 300) {
                outbox.markDelivered(event.eventId(), now);
                log.info("UCP webhook delivered: event={} order={} type={} status={}",
                        event.eventId(), event.orderId(), event.eventType(), status);
            } else {
                handleFailure(event, "HTTP " + status, now);
            }
        } catch (Exception ex) {
            handleFailure(event, ex.getClass().getSimpleName() + ": " + ex.getMessage(), now);
        }
    }

    private void handleFailure(OutboxEvent event, String error, Instant when) {
        int nextAttempts = event.attempts() + 1;
        boolean exceeded = nextAttempts >= properties.getWebhook().getMaxAttempts();
        outbox.recordAttemptFailure(event.eventId(), error, exceeded, when);
        log.warn("UCP webhook delivery failed: event={} order={} attempt={}/{} error={}",
                event.eventId(), event.orderId(), nextAttempts,
                properties.getWebhook().getMaxAttempts(), error);
    }
}
