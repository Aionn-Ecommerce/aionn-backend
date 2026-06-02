package com.aionn.sharedkernel.integration.port.notification;

/**
 * Outbound port for dispatching identity-originated notifications (OTPs,
 * security alerts).
 *
 * <p>
 * This port defines the contract for synchronous notification dispatch from the
 * Identity module.
 * It has two implementations:
 * </p>
 * <ul>
 * <li><strong>Monolith:</strong> In-memory adapter that directly calls
 * NotificationDispatchService</li>
 * <li><strong>Microservices:</strong> gRPC/HTTP client that calls Notification
 * service remotely</li>
 * </ul>
 * 
 * <p>
 * Methods are typed per use case to avoid stringly-typed payloads and enum
 * switches
 * on the consumer side.
 * </p>
 * 
 * <p>
 * <strong>Why synchronous?</strong> These operations are critical and require
 * immediate feedback:
 * <ul>
 * <li>OTP must be sent successfully before user can proceed</li>
 * <li>Password reset token must be delivered</li>
 * <li>If notification fails, the calling transaction should rollback</li>
 * </ul>
 * </p>
 */
public interface IdentityNotificationDispatcherPort {

    /**
     * Notify the user that a password reset has been requested.
     * The {@code resetToken} is delivered through the user's preferred channels
     * (typically email + SMS).
     * 
     * @param userId     the user ID
     * @param resetToken the password reset token
     * @throws NotificationException if notification dispatch fails
     */
    void sendPasswordResetRequested(String userId, String resetToken);

    /**
     * Notify the user that their password was changed (self-service or via reset).
     * 
     * @param userId      the user ID
     * @param channelHint hint about which channel triggered the change
     * @throws NotificationException if notification dispatch fails
     */
    void sendPasswordChanged(String userId, String channelHint);

    /**
     * Notify the user that their primary email was changed.
     * 
     * @param userId   the user ID
     * @param oldEmail the previous email address
     * @param newEmail the new email address
     * @throws NotificationException if notification dispatch fails
     */
    void sendEmailChanged(String userId, String oldEmail, String newEmail);

    /**
     * Notify the user that their primary phone was changed.
     * 
     * @param userId   the user ID
     * @param oldPhone the previous phone number
     * @param newPhone the new phone number
     * @throws NotificationException if notification dispatch fails
     */
    void sendPhoneChanged(String userId, String oldPhone, String newPhone);

    /**
     * Send a one-time password to an email address (e.g. email verification, change
     * confirmation).
     * 
     * @param email   the email address
     * @param otpCode the OTP code
     * @throws NotificationException if notification dispatch fails
     */
    void sendEmailOtp(String email, String otpCode);

    /**
     * Send a one-time password to a phone number (e.g. phone verification, change
     * confirmation).
     * 
     * @param phoneNumber the phone number
     * @param otpCode     the OTP code
     * @throws NotificationException if notification dispatch fails
     */
    void sendPhoneOtp(String phoneNumber, String otpCode);

    /**
     * Send a registration OTP to a phone number.
     * 
     * @param phoneNumber the phone number
     * @param otpCode     the OTP code
     * @throws NotificationException if notification dispatch fails
     */
    void sendRegistrationOtp(String phoneNumber, String otpCode);
}
