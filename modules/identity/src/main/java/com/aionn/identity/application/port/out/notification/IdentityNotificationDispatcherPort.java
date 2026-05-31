package com.aionn.identity.application.port.out.notification;

/**
 * Outbound port for dispatching identity-originated notifications (OTPs,
 * security alerts).
 *
 * <p>
 * Implementations bridge the identity bounded context to the notification
 * module.
 * Methods are typed per use case to avoid stringly-typed payloads and enum
 * switches
 * on the consumer side.
 * </p>
 */
public interface IdentityNotificationDispatcherPort {

    /**
     * Notify the user that a password reset has been requested. The
     * {@code resetToken}
     * is delivered through the user's preferred channels (typically email + SMS).
     */
    void sendPasswordResetRequested(String userId, String resetToken);

    /**
     * Notify the user that their password was changed (self-service or via reset).
     */
    void sendPasswordChanged(String userId, String channelHint);

    /**
     * Notify the user that their primary email was changed from {@code oldEmail} to
     * {@code newEmail}.
     */
    void sendEmailChanged(String userId, String oldEmail, String newEmail);

    /**
     * Notify the user that their primary phone was changed from {@code oldPhone} to
     * {@code newPhone}.
     */
    void sendPhoneChanged(String userId, String oldPhone, String newPhone);

    /**
     * Send a one-time password to {@code email} (e.g. email verification, change
     * confirmation).
     */
    void sendEmailOtp(String email, String otpCode);

    /**
     * Send a one-time password to {@code phoneNumber} (e.g. phone verification,
     * change confirmation).
     */
    void sendPhoneOtp(String phoneNumber, String otpCode);

    /** Send a registration OTP to {@code phoneNumber}. */
    void sendRegistrationOtp(String phoneNumber, String otpCode);
}
