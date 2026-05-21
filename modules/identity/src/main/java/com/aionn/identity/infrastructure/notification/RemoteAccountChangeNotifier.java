package com.aionn.identity.infrastructure.notification;

import com.aionn.identity.application.port.out.user.AccountChangeNotifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stub for the production notifier. Real implementation needs the chosen
 * email / SMS provider (SendGrid, SES, Twilio, etc.) and a templating story.
 * Until that is locked in we keep this empty so wiring is in place but no
 * misleading "fake send" happens.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "identity.notification", name = "provider", havingValue = "remote")
public class RemoteAccountChangeNotifier implements AccountChangeNotifier {

    @Override
    public void notifyEmailChanged(String userId, String oldEmail, String newEmail) {
        // TODO(notification): integrate with the chosen email provider.
        log.warn("Remote AccountChangeNotifier not wired; skipping email change notification for user {}", userId);
    }

    @Override
    public void notifyPhoneChanged(String userId, String oldPhone, String newPhone) {
        // TODO(notification): integrate with the chosen SMS provider.
        log.warn("Remote AccountChangeNotifier not wired; skipping phone change notification for user {}", userId);
    }

    @Override
    public void notifyPasswordChanged(String userId, String channelHint) {
        // TODO(notification): integrate with the chosen email provider.
        log.warn("Remote AccountChangeNotifier not wired; skipping password change notification for user {}", userId);
    }
}

