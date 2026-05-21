package com.aionn.identity.infrastructure.notification;

import com.aionn.identity.application.port.out.user.AccountChangeNotifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Default in-process notifier. Assumes the channel succeeded; useful for
 * development and integration tests where we do not want to send real email /
 * SMS but still need the audit trail.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "identity.notification", name = "provider", havingValue = "logging", matchIfMissing = true)
public class LoggingAccountChangeNotifier implements AccountChangeNotifier {

    @Override
    public void notifyEmailChanged(String userId, String oldEmail, String newEmail) {
        log.info("[ACCOUNT-CHANGE] userId={} email changed (channel notification skipped in dev)", userId);
    }

    @Override
    public void notifyPhoneChanged(String userId, String oldPhone, String newPhone) {
        log.info("[ACCOUNT-CHANGE] userId={} phone changed (channel notification skipped in dev)", userId);
    }

    @Override
    public void notifyPasswordChanged(String userId, String channelHint) {
        log.info("[ACCOUNT-CHANGE] userId={} password changed via {}", userId, channelHint);
    }
}

