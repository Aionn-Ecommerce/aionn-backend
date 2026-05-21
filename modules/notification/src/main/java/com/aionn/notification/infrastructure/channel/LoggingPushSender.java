package com.aionn.notification.infrastructure.channel;

import com.aionn.notification.application.port.out.ChannelSender;
import com.aionn.notification.domain.valueobject.NotificationChannel;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "notification.push", name = "provider", havingValue = "logging", matchIfMissing = true)
public class LoggingPushSender implements ChannelSender {

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public DeliveryResult send(DeliveryRequest request) {
        log.info("[PUSH] device={} title={} body={}", request.to(), request.subject(), request.content());
        return DeliveryResult.ok("push-" + IdGenerator.ulid());
    }
}

