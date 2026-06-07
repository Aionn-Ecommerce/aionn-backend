package com.aionn.identity.application.port.out.integration;

/**
 * Outbound port for publishing identity-related integration events to the rest
 * of the
 * platform (e.g. notification module).
 *
 * <p>
 * Application services depend on this abstraction so the outbox / messaging
 * mechanism
 * can be swapped without touching business code. Use it for fire-and-forget
 * side
 * effects only; synchronous channels (OTP, password-reset email) keep going
 * through
 * {@code IdentityNotificationDispatcherPort}.
 * </p>
 */
public interface IdentityIntegrationEventPublisherPort {

    void publishPasswordChanged(String userId, String channelHint);

    void publishEmailChanged(String userId, String oldEmail, String newEmail);

    void publishPhoneChanged(String userId, String oldPhone, String newPhone);
}
