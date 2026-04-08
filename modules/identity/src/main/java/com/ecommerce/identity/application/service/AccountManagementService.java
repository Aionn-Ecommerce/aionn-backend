package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.dto.user.command.CancelAccountDeletionCommand;
import com.ecommerce.identity.application.dto.user.command.ChangeEmailCommand;
import com.ecommerce.identity.application.dto.user.command.ChangePhoneCommand;
import com.ecommerce.identity.application.dto.user.command.RequestAccountDeletionCommand;
import com.ecommerce.identity.application.dto.user.command.RequestDataExportCommand;
import com.ecommerce.identity.application.dto.user.command.VerifyEmailCommand;
import com.ecommerce.identity.application.dto.user.view.DataExportRequestView;
import com.ecommerce.identity.application.dto.user.view.DeletionRequestView;
import com.ecommerce.identity.application.dto.user.view.UserActionOutcomeView;
import com.ecommerce.identity.application.dto.user.view.UserProfileView;
import com.ecommerce.identity.application.port.in.user.CancelAccountDeletionInputPort;
import com.ecommerce.identity.application.port.in.user.ChangeEmailInputPort;
import com.ecommerce.identity.application.port.in.user.ChangePhoneInputPort;
import com.ecommerce.identity.application.port.in.user.RequestAccountDeletionInputPort;
import com.ecommerce.identity.application.port.in.user.RequestDataExportInputPort;
import com.ecommerce.identity.application.port.in.user.VerifyEmailInputPort;
import com.ecommerce.identity.application.port.out.user.AccountDeletionPort;
import com.ecommerce.identity.application.port.out.user.DataExportPort;
import com.ecommerce.identity.application.port.out.user.EmailOtpSender;
import com.ecommerce.identity.application.port.out.user.OtpChannel;
import com.ecommerce.identity.application.port.out.user.PhoneOtpSender;
import com.ecommerce.identity.application.port.out.user.UserOtpChallengeStore;
import com.ecommerce.identity.application.port.out.user.UserOtpPurpose;
import com.ecommerce.identity.application.port.out.user.UserPersistencePort;
import com.ecommerce.identity.application.port.out.user.model.UserOtpChallenge;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.model.IdentityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Service for managing user account lifecycle operations.
 * Handles email/phone changes, account deletion requests, and data export
 * requests.
 * 
 * <p>
 * Business Rules:
 * <ul>
 * <li>Email and phone changes require OTP verification</li>
 * <li>OTP codes expire after 5 minutes and allow maximum 5 attempts</li>
 * <li>Account deletion has a 30-day grace period</li>
 * <li>Only one active data export request allowed per user</li>
 * <li>Email and phone must be unique across all users</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountManagementService implements
        VerifyEmailInputPort,
        ChangeEmailInputPort,
        ChangePhoneInputPort,
        RequestAccountDeletionInputPort,
        CancelAccountDeletionInputPort,
        RequestDataExportInputPort {

    private static final int OTP_EXPIRY_SECONDS = 300;
    private static final int OTP_MAX_ATTEMPTS = 5;
    private static final int ACCOUNT_DELETION_GRACE_DAYS = 30;

    private final UserPersistencePort userPersistencePort;
    private final EmailOtpSender emailOtpSender;
    private final PhoneOtpSender phoneOtpSender;
    private final UserOtpChallengeStore userOtpChallengeStore;
    private final AccountDeletionPort accountDeletionPort;
    private final DataExportPort dataExportPort;

    @Override
    public UserActionOutcomeView execute(VerifyEmailCommand command) {
        if ("SEND_OTP".equalsIgnoreCase(command.action())) {
            sendVerifyPrimaryEmailOtp(command.userId());
            return new UserActionOutcomeView("OTP_SENT", "OTP sent to email", null);
        }
        confirmVerifyPrimaryEmailOtp(command.userId(), command.otpCode());
        return new UserActionOutcomeView("EMAIL_VERIFIED", "Email verified", null);
    }

    @Override
    public UserActionOutcomeView execute(ChangeEmailCommand command) {
        if ("SEND_OTP".equalsIgnoreCase(command.action())) {
            requestEmailChangeOtp(command.userId(), command.newEmail());
            return new UserActionOutcomeView("OTP_SENT", "OTP sent to new email", null);
        }
        UserProfileView profile = confirmEmailChange(command.userId(), command.otpCode());
        return new UserActionOutcomeView("EMAIL_UPDATED", "Email updated", profile);
    }

    @Override
    public UserActionOutcomeView execute(ChangePhoneCommand command) {
        if ("SEND_OTP".equalsIgnoreCase(command.action())) {
            requestPhoneChangeOtp(command.userId(), command.newPhone());
            return new UserActionOutcomeView("OTP_SENT", "OTP sent to new phone", null);
        }
        UserProfileView profile = confirmPhoneChange(command.userId(), command.otpCode());
        return new UserActionOutcomeView("PHONE_UPDATED", "Phone updated", profile);
    }

    @Override
    public DeletionRequestView execute(RequestAccountDeletionCommand command) {
        return requestAccountDeletion(command.userId());
    }

    @Override
    public void execute(CancelAccountDeletionCommand command) {
        cancelAccountDeletion(command.userId());
    }

    @Override
    public DataExportRequestView execute(RequestDataExportCommand command) {
        return requestDataExport(command.userId());
    }

    /**
     * Send OTP to verify primary email address.
     *
     * @param userId the user ID
     * @throws IdentityException if user not found, inactive, or has no email
     */
    public void sendVerifyPrimaryEmailOtp(String userId) {
        IdentityUser user = getActiveUser(userId);
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IdentityException(IdentityErrorCode.EMAIL_ALREADY_EXISTS, "User has no primary email");
        }
        String otpCode = generateOtpCode();
        userOtpChallengeStore.save(new UserOtpChallenge(
                userId,
                UserOtpPurpose.VERIFY_PRIMARY_EMAIL,
                OtpChannel.EMAIL,
                user.getEmail(),
                otpCode,
                null,
                LocalDateTime.now().plusSeconds(OTP_EXPIRY_SECONDS),
                0));
        emailOtpSender.sendOtp(user.getEmail(), otpCode);
        log.info("Verification OTP sent to email for user: {}", userId);
    }

    /**
     * Confirm primary email verification with OTP.
     *
     * @param userId  the user ID
     * @param otpCode the OTP code
     * @throws IdentityException if user not found, inactive, or OTP is invalid
     */
    public void confirmVerifyPrimaryEmailOtp(String userId, String otpCode) {
        IdentityUser user = getActiveUser(userId);
        UserOtpChallenge challenge = getChallenge(
                userId,
                UserOtpPurpose.VERIFY_PRIMARY_EMAIL,
                IdentityErrorCode.EMAIL_VERIFICATION_NOT_FOUND);
        validateOtp(challenge, otpCode);
        user.verifyEmail();
        userPersistencePort.save(user);
        userOtpChallengeStore.delete(userId, UserOtpPurpose.VERIFY_PRIMARY_EMAIL);
        log.info("Email verified for user: {}", userId);
    }

    /**
     * Request email change by sending OTP to new email.
     *
     * @param userId   the user ID
     * @param newEmail the new email address
     * @throws IdentityException if user not found, inactive, email is blank, or
     *                           email already exists
     */
    public void requestEmailChangeOtp(String userId, String newEmail) {
        IdentityUser user = getActiveUser(userId);
        if (newEmail == null || newEmail.isBlank()) {
            throw new IdentityException(IdentityErrorCode.EMAIL_ALREADY_EXISTS, "New email must not be blank");
        }

        // Check if email already exists for another user
        userPersistencePort.findByIdentity(newEmail)
                .filter(existing -> !existing.getUserId().equals(user.getUserId()))
                .ifPresent(existing -> {
                    throw new IdentityException(IdentityErrorCode.EMAIL_ALREADY_EXISTS);
                });

        String otpCode = generateOtpCode();
        userOtpChallengeStore.save(new UserOtpChallenge(
                userId,
                UserOtpPurpose.CHANGE_EMAIL,
                OtpChannel.EMAIL,
                newEmail,
                otpCode,
                newEmail,
                LocalDateTime.now().plusSeconds(OTP_EXPIRY_SECONDS),
                0));
        emailOtpSender.sendOtp(newEmail, otpCode);
        log.info("Email change OTP sent to new email for user: {}", userId);
    }

    /**
     * Confirm email change with OTP.
     *
     * @param userId  the user ID
     * @param otpCode the OTP code
     * @return the updated user profile view
     * @throws IdentityException if user not found, inactive, or OTP is invalid
     */
    public UserProfileView confirmEmailChange(String userId, String otpCode) {
        IdentityUser user = getActiveUser(userId);
        UserOtpChallenge challenge = getChallenge(userId, UserOtpPurpose.CHANGE_EMAIL,
                IdentityErrorCode.EMAIL_CHANGE_NOT_FOUND);
        validateOtp(challenge, otpCode);
        user.updateEmail(challenge.pendingValue());
        IdentityUser saved = userPersistencePort.save(user);
        userOtpChallengeStore.delete(userId, UserOtpPurpose.CHANGE_EMAIL);
        log.info("Email changed for user: {}", userId);
        return toProfileView(saved);
    }

    /**
     * Request phone change by sending OTP to new phone.
     *
     * @param userId   the user ID
     * @param newPhone the new phone number
     * @throws IdentityException if user not found, inactive, phone is blank, or
     *                           phone already exists
     */
    public void requestPhoneChangeOtp(String userId, String newPhone) {
        getActiveUser(userId); // Validate user is active
        if (newPhone == null || newPhone.isBlank()) {
            throw new IdentityException(IdentityErrorCode.PHONE_ALREADY_EXISTS, "New phone must not be blank");
        }

        // Check if phone already exists
        if (userPersistencePort.existsByPhone(newPhone)) {
            throw new IdentityException(IdentityErrorCode.PHONE_ALREADY_EXISTS);
        }

        String otpCode = generateOtpCode();
        userOtpChallengeStore.save(new UserOtpChallenge(
                userId,
                UserOtpPurpose.CHANGE_PHONE,
                OtpChannel.PHONE,
                newPhone,
                otpCode,
                newPhone,
                LocalDateTime.now().plusSeconds(OTP_EXPIRY_SECONDS),
                0));
        phoneOtpSender.sendOtp(newPhone, otpCode);
        log.info("Phone change OTP sent to new phone for user: {}", userId);
    }

    /**
     * Confirm phone change with OTP.
     *
     * @param userId  the user ID
     * @param otpCode the OTP code
     * @return the updated user profile view
     * @throws IdentityException if user not found, inactive, or OTP is invalid
     */
    public UserProfileView confirmPhoneChange(String userId, String otpCode) {
        IdentityUser user = getActiveUser(userId);
        UserOtpChallenge challenge = getChallenge(userId, UserOtpPurpose.CHANGE_PHONE,
                IdentityErrorCode.PHONE_CHANGE_NOT_FOUND);
        validateOtp(challenge, otpCode);
        user.updatePhone(challenge.pendingValue());
        IdentityUser saved = userPersistencePort.save(user);
        userOtpChallengeStore.delete(userId, UserOtpPurpose.CHANGE_PHONE);
        log.info("Phone changed for user: {}", userId);
        return toProfileView(saved);
    }

    /**
     * Request account deletion with grace period.
     *
     * @param userId the user ID
     * @return the deletion request view
     * @throws IdentityException if user not found, inactive, or deletion already
     *                           requested
     */
    public DeletionRequestView requestAccountDeletion(String userId) {
        getActiveUser(userId); // Validate user is active
        if (accountDeletionPort.findPendingByUserId(userId).isPresent()) {
            throw new IdentityException(IdentityErrorCode.ACCOUNT_DELETION_ALREADY_REQUESTED);
        }
        LocalDateTime scheduledDeletionAt = LocalDateTime.now().plusDays(ACCOUNT_DELETION_GRACE_DAYS);
        DeletionRequestView result = accountDeletionPort.save(userId, scheduledDeletionAt);
        log.info("Account deletion requested for user: {}, scheduled at: {}", userId, scheduledDeletionAt);
        return result;
    }

    /**
     * Cancel account deletion request.
     *
     * @param userId the user ID
     * @throws IdentityException if user not found, inactive, or no pending deletion
     *                           request
     */
    public void cancelAccountDeletion(String userId) {
        getActiveUser(userId);
        if (accountDeletionPort.findPendingByUserId(userId).isEmpty()) {
            throw new IdentityException(IdentityErrorCode.ACCOUNT_DELETION_NOT_FOUND);
        }
        accountDeletionPort.cancel(userId);
        log.info("Account deletion canceled for user: {}", userId);
    }

    /**
     * Request data export.
     *
     * @param userId the user ID
     * @return the data export request view
     * @throws IdentityException if user not found, inactive, or export already in
     *                           progress
     */
    public DataExportRequestView requestDataExport(String userId) {
        getActiveUser(userId); // Validate user is active
        if (dataExportPort.hasActiveRequest(userId)) {
            throw new IdentityException(IdentityErrorCode.DATA_EXPORT_ALREADY_IN_PROGRESS);
        }
        DataExportRequestView result = dataExportPort.save(userId);
        log.info("Data export requested for user: {}", userId);
        return result;
    }

    private UserOtpChallenge getChallenge(String userId, UserOtpPurpose purpose, IdentityErrorCode notFoundCode) {
        return userOtpChallengeStore.find(userId, purpose)
                .orElseThrow(() -> new IdentityException(notFoundCode));
    }

    private void validateOtp(UserOtpChallenge challenge, String otpCode) {
        if (otpCode == null || otpCode.isBlank()) {
            throw new IdentityException(IdentityErrorCode.OTP_REQUIRED);
        }
        if (challenge.expiresAt().isBefore(LocalDateTime.now())) {
            userOtpChallengeStore.delete(challenge.userId(), challenge.purpose());
            throw new IdentityException(IdentityErrorCode.OTP_EXPIRED);
        }
        if (!challenge.otpCode().equals(otpCode)) {
            int attempts = challenge.attempts() + 1;
            if (attempts >= OTP_MAX_ATTEMPTS) {
                userOtpChallengeStore.delete(challenge.userId(), challenge.purpose());
                throw new IdentityException(IdentityErrorCode.OTP_ATTEMPTS_EXCEEDED);
            }
            userOtpChallengeStore.save(challenge.withAttempts(attempts));
            throw new IdentityException(IdentityErrorCode.OTP_INVALID);
        }
    }

    private IdentityUser getActiveUser(String userId) {
        IdentityUser user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
        if (!user.isActive()) {
            throw new IdentityException(IdentityErrorCode.USER_INACTIVE);
        }
        return user;
    }

    private UserProfileView toProfileView(IdentityUser user) {
        return new UserProfileView(
                user.getUserId(),
                user.getEmail(),
                user.getPhone(),
                user.getUsername(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()),
                user.getStatus().name(),
                user.getEmailVerifiedAt(),
                user.getPhoneVerifiedAt(),
                user.getCreatedAt());
    }

    private String generateOtpCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
    }
}
