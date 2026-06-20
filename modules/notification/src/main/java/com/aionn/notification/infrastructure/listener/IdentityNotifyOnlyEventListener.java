package com.aionn.notification.infrastructure.listener;

import com.aionn.notification.application.dto.notification.command.NotificationCommands;
import com.aionn.notification.application.service.NotificationDispatchService;
import com.aionn.notification.domain.valueobject.NotificationCategory;
import com.aionn.notification.domain.valueobject.NotificationChannel;
import com.aionn.sharedkernel.integration.event.identity.EmailChangedIntegrationEvent;
import com.aionn.sharedkernel.integration.event.identity.PasswordChangedIntegrationEvent;
import com.aionn.sharedkernel.integration.event.identity.PhoneChangedIntegrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Listens to "notify-only" identity integration events and dispatches the
 * notification asynchronously (best-effort). Failure here never affects the
 * Identity transaction because the event is consumed after Identity's
 * commit.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdentityNotifyOnlyEventListener {

    private static final String EVENT_PASSWORD_CHANGED = "IDENTITY_PASSWORD_CHANGED";
    private static final String EVENT_EMAIL_CHANGED = "IDENTITY_EMAIL_CHANGED";
    private static final String EVENT_PHONE_CHANGED = "IDENTITY_PHONE_CHANGED";

    private static final List<NotificationChannel> CHANNELS_PASSWORD_CHANGED = List.of(
            NotificationChannel.EMAIL, NotificationChannel.IN_APP, NotificationChannel.PUSH);
    private static final List<NotificationChannel> CHANNELS_EMAIL_CHANGED = List.of(
            NotificationChannel.EMAIL, NotificationChannel.IN_APP, NotificationChannel.PUSH);
    private static final List<NotificationChannel> CHANNELS_PHONE_CHANGED = List.of(
            NotificationChannel.SMS, NotificationChannel.IN_APP, NotificationChannel.PUSH);

    private final NotificationDispatchService notificationDispatchService;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPasswordChanged(PasswordChangedIntegrationEvent event) {
        try {
            notificationDispatchService.sendByEvent(new NotificationCommands.SendByEvent(
                    event.userId(),
                    EVENT_PASSWORD_CHANGED,
                    NotificationCategory.SECURITY,
                    CHANNELS_PASSWORD_CHANGED,
                    null,
                    null,
                    Map.of("channelHint", placeholder(event.channelHint()))));
        } catch (RuntimeException ex) {
            log.error("Failed to dispatch password-changed notification for user {}", event.userId(), ex);
        }
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onEmailChanged(EmailChangedIntegrationEvent event) {
        try {
            notificationDispatchService.sendByEvent(new NotificationCommands.SendByEvent(
                    event.userId(),
                    EVENT_EMAIL_CHANGED,
                    NotificationCategory.SECURITY,
                    CHANNELS_EMAIL_CHANGED,
                    null,
                    null,
                    Map.of("oldEmail", placeholder(event.oldEmail()),
                            "newEmail", placeholder(event.newEmail()))));
        } catch (RuntimeException ex) {
            log.error("Failed to dispatch email-changed notification for user {}", event.userId(), ex);
        }
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPhoneChanged(PhoneChangedIntegrationEvent event) {
        try {
            notificationDispatchService.sendByEvent(new NotificationCommands.SendByEvent(
                    event.userId(),
                    EVENT_PHONE_CHANGED,
                    NotificationCategory.SECURITY,
                    CHANNELS_PHONE_CHANGED,
                    null,
                    null,
                    Map.of("oldPhone", placeholder(event.oldPhone()),
                            "newPhone", placeholder(event.newPhone()))));
        } catch (RuntimeException ex) {
            log.error("Failed to dispatch phone-changed notification for user {}", event.userId(), ex);
        }
    }

    private static String placeholder(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }
}
