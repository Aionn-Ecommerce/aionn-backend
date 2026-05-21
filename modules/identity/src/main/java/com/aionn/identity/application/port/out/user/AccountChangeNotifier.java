package com.aionn.identity.application.port.out.user;

/**
 * Notification side effects fired after sensitive account changes
 * (email/phone/password update). Implementation strategy is two-layer per
 * project convention:
 *
 * <ul>
 * <li>{@code LoggingAccountChangeNotifier} - logs to console; assume-correct
 * default for dev / tests.</li>
 * <li>{@code RemoteAccountChangeNotifier} - real provider (e.g. SendGrid /
 * Twilio). Stub today, wire up after the provider is locked in.</li>
 * </ul>
 */
public interface AccountChangeNotifier {

    /** Notify the user via the previous email that their email was changed. */
    void notifyEmailChanged(String userId, String oldEmail, String newEmail);

    /** Notify the user via the previous phone that their phone was changed. */
    void notifyPhoneChanged(String userId, String oldPhone, String newPhone);

    /** Notify the user that their password was changed/reset. */
    void notifyPasswordChanged(String userId, String channelHint);
}

