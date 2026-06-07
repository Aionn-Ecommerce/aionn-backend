package com.aionn.notification.infrastructure.recipient;

import com.aionn.notification.application.port.out.DeviceTokenRepository;
import com.aionn.notification.application.port.out.RecipientResolver;
import com.aionn.notification.domain.model.DeviceToken;
import com.aionn.notification.domain.valueobject.NotificationChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notification.recipient", name = "provider", havingValue = "stub", matchIfMissing = true)
public class StubRecipientResolver implements RecipientResolver {

    private final DeviceTokenRepository deviceTokenRepository;

    @Override
    public String resolve(String userId, NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> userId + "@stub.test";
            case SMS -> "+8490" + Math.abs(userId.hashCode() % 10_000_000);
            case PUSH -> {
                List<DeviceToken> tokens = deviceTokenRepository.findActiveByUserId(userId);
                yield tokens.isEmpty() ? "no-device-token" : tokens.get(0).getDeviceToken();
            }
            case IN_APP -> "in-app:" + userId;
        };
    }
}

