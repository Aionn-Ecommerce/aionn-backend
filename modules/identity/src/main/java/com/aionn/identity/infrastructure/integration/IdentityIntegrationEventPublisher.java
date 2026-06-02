package com.aionn.identity.infrastructure.integration;

import com.aionn.identity.application.port.out.integration.IdentityIntegrationEventPublisherPort;
import com.aionn.sharedkernel.integration.event.identity.EmailChangedIntegrationEvent;
import com.aionn.sharedkernel.integration.event.identity.PasswordChangedIntegrationEvent;
import com.aionn.sharedkernel.integration.event.identity.PhoneChangedIntegrationEvent;
import com.aionn.sharedkernel.integration.publisher.IntegrationEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Adapter that publishes Identity integration events through the shared-kernel
 * outbox publisher.
 *
 * <p>
 * Application services interact with this through
 * {@link IdentityIntegrationEventPublisherPort} so the messaging mechanism can
 * change
 * (e.g. swap to a different transport, add tracing, batch publication) without
 * leaking
 * into business code.
 * </p>
 *
 * <p>
 * Used only for fire-and-forget notifications where the caller does not need to
 * know
 * the result and the user is not waiting for an OTP-style payload. Synchronous
 * flows
 * (OTP delivery, password reset token) keep using
 * {@link com.aionn.sharedkernel.integration.port.notification.IdentityNotificationDispatcherPort}.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdentityIntegrationEventPublisher implements IdentityIntegrationEventPublisherPort {

    private final IntegrationEventPublisher integrationEventPublisher;

    @Override
    public void publishPasswordChanged(String userId, String channelHint) {
        log.debug("Publishing PasswordChangedIntegrationEvent for user: {}", userId);
        integrationEventPublisher.publish(new PasswordChangedIntegrationEvent(
                null, userId, channelHint, Instant.now()));
    }

    @Override
    public void publishEmailChanged(String userId, String oldEmail, String newEmail) {
        log.debug("Publishing EmailChangedIntegrationEvent for user: {}", userId);
        integrationEventPublisher.publish(new EmailChangedIntegrationEvent(
                null, userId, oldEmail, newEmail, Instant.now()));
    }

    @Override
    public void publishPhoneChanged(String userId, String oldPhone, String newPhone) {
        log.debug("Publishing PhoneChangedIntegrationEvent for user: {}", userId);
        integrationEventPublisher.publish(new PhoneChangedIntegrationEvent(
                null, userId, oldPhone, newPhone, Instant.now()));
    }
}
