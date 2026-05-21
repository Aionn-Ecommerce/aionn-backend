package com.aionn.notification.infrastructure.channel;

import com.aionn.notification.application.port.out.ChannelSender;
import com.aionn.notification.domain.valueobject.NotificationChannel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "notification.sms", name = "provider", havingValue = "remote")
public class RemoteSmsSender implements ChannelSender {

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS;
    }

    @Override
    public DeliveryResult send(DeliveryRequest request) {
        return DeliveryResult.failed("NOT_IMPLEMENTED", "Remote SMS sender is not implemented yet");
    }
}

