package com.aionn.notification.infrastructure.channel;

import com.aionn.notification.application.port.out.ChannelSender;
import com.aionn.notification.domain.valueobject.NotificationChannel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "notification.email", name = "provider", havingValue = "remote")
public class RemoteEmailSender implements ChannelSender {

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public DeliveryResult send(DeliveryRequest request) {
        return DeliveryResult.failed("NOT_IMPLEMENTED", "Remote email sender is not implemented yet");
    }
}

