package com.aionn.integration.notification.sender;

import com.aionn.identity.application.port.out.notification.IdentityNotificationDispatcherPort;
import com.aionn.notification.application.dto.notification.command.NotificationCommands;
import com.aionn.notification.application.service.NotificationDispatchService;
import com.aionn.notification.domain.valueobject.NotificationCategory;
import com.aionn.notification.domain.valueobject.NotificationChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * App-side adapter that fulfils the identity
 * {@link IdentityNotificationDispatcherPort}
 * by delegating to the notification module's dispatch service.
 *
 * <p>
 * Each dispatcher method maps directly to a notification template id (e.g.
 * {@code IDENTITY_EMAIL_OTP}) plus the channel(s) appropriate for the use case.
 * The previous {@code IdentityNotificationRequestedEvent} fan-out lived here as
 * a {@link org.springframework.context.event.EventListener}; this adapter
 * replaces
 * that indirection.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class IdentityNotificationDispatcherAdapter implements IdentityNotificationDispatcherPort {

    private static final String EVENT_PASSWORD_RESET_REQUESTED = "IDENTITY_PASSWORD_RESET_REQUESTED";
    private static final String EVENT_PASSWORD_CHANGED = "IDENTITY_PASSWORD_CHANGED";
    private static final String EVENT_EMAIL_CHANGED = "IDENTITY_EMAIL_CHANGED";
    private static final String EVENT_PHONE_CHANGED = "IDENTITY_PHONE_CHANGED";
    private static final String EVENT_EMAIL_OTP = "IDENTITY_EMAIL_OTP";
    private static final String EVENT_PHONE_OTP = "IDENTITY_PHONE_OTP";
    private static final String EVENT_REGISTRATION_OTP = "IDENTITY_REGISTRATION_OTP";

    private static final String KEY_EMAIL_OTP = "email-otp";
    private static final String KEY_PHONE_OTP = "phone-otp";
    private static final String KEY_REGISTRATION_OTP = "registration-otp";

    private static final List<NotificationChannel> CHANNELS_PASSWORD_RESET = List.of(NotificationChannel.EMAIL,
            NotificationChannel.SMS);
    private static final List<NotificationChannel> CHANNELS_PASSWORD_CHANGED = List.of(NotificationChannel.EMAIL,
            NotificationChannel.IN_APP, NotificationChannel.PUSH);
    private static final List<NotificationChannel> CHANNELS_EMAIL_CHANGED = List.of(NotificationChannel.EMAIL,
            NotificationChannel.IN_APP, NotificationChannel.PUSH);
    private static final List<NotificationChannel> CHANNELS_PHONE_CHANGED = List.of(NotificationChannel.SMS,
            NotificationChannel.IN_APP, NotificationChannel.PUSH);

    private static final HexFormat HEX = HexFormat.of();

    private final NotificationDispatchService notificationDispatchService;

    @Override
    public void sendPasswordResetRequested(String userId, String resetToken) {
        sendByEvent(
                userId,
                EVENT_PASSWORD_RESET_REQUESTED,
                CHANNELS_PASSWORD_RESET,
                Map.of("resetToken", placeholder(resetToken)));
    }

    @Override
    public void sendPasswordChanged(String userId, String channelHint) {
        sendByEvent(
                userId,
                EVENT_PASSWORD_CHANGED,
                CHANNELS_PASSWORD_CHANGED,
                Map.of("channelHint", placeholder(channelHint)));
    }

    @Override
    public void sendEmailChanged(String userId, String oldEmail, String newEmail) {
        sendByEvent(
                userId,
                EVENT_EMAIL_CHANGED,
                CHANNELS_EMAIL_CHANGED,
                Map.of("oldEmail", placeholder(oldEmail), "newEmail", placeholder(newEmail)));
    }

    @Override
    public void sendPhoneChanged(String userId, String oldPhone, String newPhone) {
        sendByEvent(
                userId,
                EVENT_PHONE_CHANGED,
                CHANNELS_PHONE_CHANGED,
                Map.of("oldPhone", placeholder(oldPhone), "newPhone", placeholder(newPhone)));
    }

    @Override
    public void sendEmailOtp(String email, String otpCode) {
        sendDirect(
                recipientKey(KEY_EMAIL_OTP, email),
                EVENT_EMAIL_OTP,
                NotificationChannel.EMAIL,
                email,
                Map.of("otpCode", otpCode));
    }

    @Override
    public void sendPhoneOtp(String phoneNumber, String otpCode) {
        sendDirect(
                recipientKey(KEY_PHONE_OTP, phoneNumber),
                EVENT_PHONE_OTP,
                NotificationChannel.SMS,
                phoneNumber,
                Map.of("otpCode", otpCode));
    }

    @Override
    public void sendRegistrationOtp(String phoneNumber, String otpCode) {
        sendDirect(
                recipientKey(KEY_REGISTRATION_OTP, phoneNumber),
                EVENT_REGISTRATION_OTP,
                NotificationChannel.SMS,
                phoneNumber,
                Map.of("otpCode", otpCode));
    }

    private void sendByEvent(
            String userId,
            String eventType,
            List<NotificationChannel> channels,
            Map<String, String> context) {
        notificationDispatchService.sendByEvent(new NotificationCommands.SendByEvent(
                userId,
                eventType,
                NotificationCategory.SECURITY,
                channels,
                null,
                null,
                context));
    }

    private void sendDirect(
            String userId,
            String eventType,
            NotificationChannel channel,
            String recipient,
            Map<String, String> context) {
        notificationDispatchService.sendDirectByEvent(new NotificationCommands.SendDirectByEvent(
                userId,
                eventType,
                NotificationCategory.SECURITY,
                channel,
                recipient,
                null,
                null,
                context));
    }

    private static String recipientKey(String prefix, String value) {
        return prefix + ":" + digest(value);
    }

    private static String digest(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HEX.formatHex(hash, 0, 16);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private static String placeholder(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }
}
