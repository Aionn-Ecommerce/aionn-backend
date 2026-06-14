package com.aionn.ucp.infrastructure.webhook;

import com.aionn.ucp.application.port.out.OrderEventOutboxPort;
import com.aionn.ucp.infrastructure.config.UcpProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ucp.webhook", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OrderWebhookDeliveryScheduler {

    private final OrderEventOutboxPort outbox;
    private final OrderWebhookDeliveryWorker worker;
    private final UcpProperties properties;

    @Scheduled(fixedDelayString = "${ucp.webhook.delay-ms:30000}")
    public void deliverPending() {
        try {
            List<OrderEventOutboxPort.OutboxEvent> pending = outbox.findPending(
                    properties.getWebhook().getBatchSize(),
                    properties.getWebhook().getMaxAttempts());
            if (pending.isEmpty()) {
                return;
            }
            for (OrderEventOutboxPort.OutboxEvent event : pending) {
                worker.deliverOne(event);
            }
            log.debug("UCP webhook delivery cycle: {} event(s)", pending.size());
        } catch (Exception ex) {
            log.error("UCP webhook delivery cycle failed", ex);
        }
    }
}
