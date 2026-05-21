package com.aionn.notification.infrastructure.channel;

import com.aionn.notification.application.port.out.ChannelSender;
import com.aionn.notification.domain.valueobject.NotificationChannel;
import org.springframework.stereotype.Component;

/**
 * In-app notifications are delivered by storing the notification row;
 * dispatch is a no-op success â€” the user fetches them via REST.
 */
@Component
public class InAppSender implements ChannelSender {

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public DeliveryResult send(DeliveryRequest request) {
        return DeliveryResult.ok("in-app:" + request.notiId());
    }
}

