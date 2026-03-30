package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.dto.user.CancelAccountDeletionCommand;
import com.ecommerce.identity.application.dto.user.ChangeEmailCommand;
import com.ecommerce.identity.application.dto.user.ChangePhoneCommand;
import com.ecommerce.identity.application.dto.user.DataExportRequestView;
import com.ecommerce.identity.application.dto.user.DeletionRequestView;
import com.ecommerce.identity.application.dto.user.GetMyProfileQuery;
import com.ecommerce.identity.application.dto.user.RequestAccountDeletionCommand;
import com.ecommerce.identity.application.dto.user.RequestDataExportCommand;
import com.ecommerce.identity.application.dto.user.UpdateAvatarCommand;
import com.ecommerce.identity.application.dto.user.UpdateDisplayNameCommand;
import com.ecommerce.identity.application.dto.user.UserActionOutcomeView;
import com.ecommerce.identity.application.dto.user.UserProfileView;
import com.ecommerce.identity.application.dto.user.VerifyEmailCommand;
import com.ecommerce.identity.application.port.in.user.CancelAccountDeletionInputPort;
import com.ecommerce.identity.application.port.in.user.ChangeEmailInputPort;
import com.ecommerce.identity.application.port.in.user.ChangePhoneInputPort;
import com.ecommerce.identity.application.port.in.user.GetMyProfileInputPort;
import com.ecommerce.identity.application.port.in.user.RequestAccountDeletionInputPort;
import com.ecommerce.identity.application.port.in.user.RequestDataExportInputPort;
import com.ecommerce.identity.application.port.in.user.UpdateAvatarInputPort;
import com.ecommerce.identity.application.port.in.user.UpdateDisplayNameInputPort;
import com.ecommerce.identity.application.port.in.user.UserServiceInputPort;
import com.ecommerce.identity.application.port.in.user.VerifyEmailInputPort;
import com.ecommerce.identity.application.port.out.user.EmailOtpSender;
import com.ecommerce.identity.application.port.out.user.OtpChannel;
import com.ecommerce.identity.application.port.out.user.PhoneOtpSender;
import com.ecommerce.identity.application.port.out.user.UserOtpChallengeStore;
import com.ecommerce.identity.application.port.out.user.UserOtpPurpose;
import com.ecommerce.identity.application.port.out.user.model.UserOtpChallenge;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.valueobject.DataExportStatus;
import com.ecommerce.identity.domain.valueobject.UserStatus;
import com.ecommerce.identity.infrastructure.persistence.entity.AccountDeletionRequestEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.DataExportRequestEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import com.ecommerce.identity.infrastructure.persistence.repository.account.AccountDeletionRequestRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.account.DataExportRequestRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
import com.ecommerce.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements
        UserServiceInputPort,
        GetMyProfileInputPort,
        VerifyEmailInputPort,
        UpdateDisplayNameInputPort,
        UpdateAvatarInputPort,
        ChangeEmailInputPort,
        ChangePhoneInputPort,
        RequestAccountDeletionInputPort,
        CancelAccountDeletionInputPort,
        RequestDataExportInputPort {

    private static final int OTP_EXPIRY_SECONDS = 300;
    private static final int OTP_MAX_ATTEMPTS = 5;
    private static final int ACCOUNT_DELETION_GRACE_DAYS = 30;

    private final UserRepository userRepository;
    private final EmailOtpSender emailOtpSender;
    private final PhoneOtpSender phoneOtpSender;
    private final UserOtpChallengeStore userOtpChallengeStore;
    private final AccountDeletionRequestRepository accountDeletionRequestRepository;
    private final DataExportRequestRepository dataExportRequestRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileView getMyProfile(String userId) {
        return toProfileView(getActiveUser(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileView execute(GetMyProfileQuery query) {
        return getMyProfile(query.userId());
    }

    @Override
    @Transactional
    public void sendVerifyPrimaryEmailOtp(String userId) {
        UserEntity user = getActiveUser(userId);
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
    }

    @Override
    @Transactional
    public void confirmVerifyPrimaryEmailOtp(String userId, String otpCode) {
        UserEntity user = getActiveUser(userId);
        UserOtpChallenge challenge = getChallenge(
                userId,
                UserOtpPurpose.VERIFY_PRIMARY_EMAIL,
                IdentityErrorCode.EMAIL_VERIFICATION_NOT_FOUND);
        validateOtp(challenge, otpCode);
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);
        userOtpChallengeStore.delete(userId, UserOtpPurpose.VERIFY_PRIMARY_EMAIL);
    }

    @Override
    @Transactional
    public UserActionOutcomeView execute(VerifyEmailCommand command) {
        if ("SEND_OTP".equalsIgnoreCase(command.action())) {
            sendVerifyPrimaryEmailOtp(command.userId());
            return new UserActionOutcomeView("OTP_SENT", "OTP sent to email", null);
        }
        confirmVerifyPrimaryEmailOtp(command.userId(), command.otpCode());
        return new UserActionOutcomeView("EMAIL_VERIFIED", "Email verified", null);
    }

    @Override
    @Transactional
    public UserProfileView updateDisplayName(String userId, String displayName) {
        UserEntity user = getActiveUser(userId);
        user.setDisplayName(displayName);
        return toProfileView(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserProfileView execute(UpdateDisplayNameCommand command) {
        return updateDisplayName(command.userId(), command.displayName());
    }

    @Override
    @Transactional
    public UserProfileView updateAvatar(String userId, String avatarUrl) {
        UserEntity user = getActiveUser(userId);
        if (avatarUrl == null || avatarUrl.isBlank() || !avatarUrl.startsWith("http")) {
            throw new IdentityException(IdentityErrorCode.AVATAR_URL_INVALID);
        }
        user.setAvatarUrl(avatarUrl.trim());
        return toProfileView(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserProfileView execute(UpdateAvatarCommand command) {
        return updateAvatar(command.userId(), command.avatarUrl());
    }

    @Override
    @Transactional
    public void requestEmailChangeOtp(String userId, String newEmail) {
        UserEntity user = getActiveUser(userId);
        if (newEmail == null || newEmail.isBlank()) {
            throw new IdentityException(IdentityErrorCode.EMAIL_ALREADY_EXISTS, "New email must not be blank");
        }
        userRepository.findByEmailIgnoreCase(newEmail)
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
    }

    @Override
    @Transactional
    public UserProfileView confirmEmailChange(String userId, String otpCode) {
        UserEntity user = getActiveUser(userId);
        UserOtpChallenge challenge = getChallenge(userId, UserOtpPurpose.CHANGE_EMAIL, IdentityErrorCode.EMAIL_CHANGE_NOT_FOUND);
        validateOtp(challenge, otpCode);
        user.setEmail(challenge.pendingValue());
        UserProfileView profile = toProfileView(userRepository.save(user));
        userOtpChallengeStore.delete(userId, UserOtpPurpose.CHANGE_EMAIL);
        return profile;
    }

    @Override
    @Transactional
    public UserActionOutcomeView execute(ChangeEmailCommand command) {
        if ("SEND_OTP".equalsIgnoreCase(command.action())) {
            requestEmailChangeOtp(command.userId(), command.newEmail());
            return new UserActionOutcomeView("OTP_SENT", "OTP sent to new email", null);
        }
        UserProfileView profile = confirmEmailChange(command.userId(), command.otpCode());
        return new UserActionOutcomeView("EMAIL_UPDATED", "Email updated", profile);
    }

    @Override
    @Transactional
    public void requestPhoneChangeOtp(String userId, String newPhone) {
        UserEntity user = getActiveUser(userId);
        if (newPhone == null || newPhone.isBlank()) {
            throw new IdentityException(IdentityErrorCode.PHONE_ALREADY_EXISTS, "New phone must not be blank");
        }
        userRepository.findByPhone(newPhone)
                .filter(existing -> !existing.getUserId().equals(user.getUserId()))
                .ifPresent(existing -> {
                    throw new IdentityException(IdentityErrorCode.PHONE_ALREADY_EXISTS);
                });

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
    }

    @Override
    @Transactional
    public UserProfileView confirmPhoneChange(String userId, String otpCode) {
        UserEntity user = getActiveUser(userId);
        UserOtpChallenge challenge = getChallenge(userId, UserOtpPurpose.CHANGE_PHONE, IdentityErrorCode.PHONE_CHANGE_NOT_FOUND);
        validateOtp(challenge, otpCode);
        user.setPhone(challenge.pendingValue());
        UserProfileView profile = toProfileView(userRepository.save(user));
        userOtpChallengeStore.delete(userId, UserOtpPurpose.CHANGE_PHONE);
        return profile;
    }

    @Override
    @Transactional
    public UserActionOutcomeView execute(ChangePhoneCommand command) {
        if ("SEND_OTP".equalsIgnoreCase(command.action())) {
            requestPhoneChangeOtp(command.userId(), command.newPhone());
            return new UserActionOutcomeView("OTP_SENT", "OTP sent to new phone", null);
        }
        UserProfileView profile = confirmPhoneChange(command.userId(), command.otpCode());
        return new UserActionOutcomeView("PHONE_UPDATED", "Phone updated", profile);
    }

    @Override
    @Transactional
    public DeletionRequestView requestAccountDeletion(String userId) {
        UserEntity user = getActiveUser(userId);
        if (accountDeletionRequestRepository.findByUser_UserIdAndStatus(userId, "PENDING").isPresent()) {
            throw new IdentityException(IdentityErrorCode.ACCOUNT_DELETION_ALREADY_REQUESTED);
        }
        AccountDeletionRequestEntity request = AccountDeletionRequestEntity.builder()
                .deletionRequestId(IdGenerator.ulid())
                .user(user)
                .status("PENDING")
                .requestedAt(LocalDateTime.now())
                .scheduledDeletionAt(LocalDateTime.now().plusDays(ACCOUNT_DELETION_GRACE_DAYS))
                .build();
        AccountDeletionRequestEntity saved = accountDeletionRequestRepository.save(request);
        return new DeletionRequestView(
                saved.getDeletionRequestId(),
                saved.getStatus(),
                saved.getRequestedAt(),
                saved.getScheduledDeletionAt());
    }

    @Override
    @Transactional
    public DeletionRequestView execute(RequestAccountDeletionCommand command) {
        return requestAccountDeletion(command.userId());
    }

    @Override
    @Transactional
    public void cancelAccountDeletion(String userId) {
        AccountDeletionRequestEntity request = accountDeletionRequestRepository
                .findByUser_UserIdAndStatus(userId, "PENDING")
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.ACCOUNT_DELETION_NOT_FOUND));
        request.setStatus("CANCELED");
        request.setCanceledAt(LocalDateTime.now());
        accountDeletionRequestRepository.save(request);
    }

    @Override
    @Transactional
    public void execute(CancelAccountDeletionCommand command) {
        cancelAccountDeletion(command.userId());
    }

    @Override
    @Transactional
    public DataExportRequestView requestDataExport(String userId) {
        UserEntity user = getActiveUser(userId);
        if (dataExportRequestRepository.existsByUser_UserIdAndStatusIn(
                userId, java.util.List.of(DataExportStatus.REQUESTED.name(), DataExportStatus.PROCESSING.name()))) {
            throw new IdentityException(IdentityErrorCode.DATA_EXPORT_ALREADY_IN_PROGRESS);
        }
        DataExportRequestEntity request = DataExportRequestEntity.builder()
                .exportRequestId(IdGenerator.ulid())
                .user(user)
                .status(DataExportStatus.REQUESTED.name())
                .requestedAt(LocalDateTime.now())
                .build();
        DataExportRequestEntity saved = dataExportRequestRepository.save(request);
        return new DataExportRequestView(saved.getExportRequestId(), saved.getStatus(), saved.getRequestedAt());
    }

    @Override
    @Transactional
    public DataExportRequestView execute(RequestDataExportCommand command) {
        return requestDataExport(command.userId());
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

    private UserEntity getActiveUser(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IdentityException(IdentityErrorCode.USER_INACTIVE);
        }
        return user;
    }

    private UserProfileView toProfileView(UserEntity user) {
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
