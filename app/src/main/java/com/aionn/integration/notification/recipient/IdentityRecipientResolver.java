package com.aionn.integration.notification.recipient;

import com.aionn.identity.application.port.out.user.UserPersistencePort;
import com.aionn.notification.application.port.out.DeviceTokenRepository;
import com.aionn.notification.application.port.out.RecipientResolver;
import com.aionn.notification.domain.exception.NotificationErrorCode;
import com.aionn.notification.domain.exception.NotificationException;
import com.aionn.notification.domain.model.DeviceToken;
import com.aionn.notification.domain.valueobject.NotificationChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notification.recipient", name = "provider", havingValue = "identity", matchIfMissing = true)
public class IdentityRecipientResolver implements RecipientResolver {

    private final UserPersistencePort userPersistencePort;
    private final DeviceTokenRepository deviceTokenRepository;

    @Override
    public String resolve(String userId, NotificationChannel channel) {
        if (channel == NotificationChannel.IN_APP) {
            return "in-app:" + userId;
        }

        var user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new NotificationException(
                        NotificationErrorCode.RECIPIENT_NOT_FOUND,
                        "No identity user found for notification userId=" + userId));

        return switch (channel) {
            case EMAIL -> requireValue(user.getEmail(), userId, "email");
            case SMS -> requireValue(user.getPhone(), userId, "phone");
            case PUSH -> deviceTokenRepository.findActiveByUserId(userId).stream()
                    .findFirst()
                    .map(DeviceToken::getDeviceToken)
                    .filter(token -> !token.isBlank())
                    .orElseThrow(() -> new NotificationException(
                            NotificationErrorCode.RECIPIENT_NOT_FOUND,
                            "No active device token found for userId=" + userId));
            case IN_APP -> "in-app:" + userId;
        };
    }

    private static String requireValue(String value, String userId, String channelName) {
        if (value == null || value.isBlank()) {
            throw new NotificationException(
                    NotificationErrorCode.RECIPIENT_NOT_FOUND,
                    "User " + userId + " has no " + channelName + " recipient");
        }
        return value;
    }
}
