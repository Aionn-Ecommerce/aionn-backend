package com.aionn.notification.infrastructure.recipient;

import com.aionn.notification.application.port.out.RecipientResolver;
import com.aionn.notification.domain.valueobject.NotificationChannel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "notification.recipient", name = "provider", havingValue = "remote")
public class RemoteRecipientResolver implements RecipientResolver {

    @Override
    public String resolve(String userId, NotificationChannel channel) {
        throw new UnsupportedOperationException("Remote recipient resolver is not implemented yet");
    }
}

