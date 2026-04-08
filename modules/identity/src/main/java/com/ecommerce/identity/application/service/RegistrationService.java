package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.dto.registration.command.CompleteRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.command.InitiateRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.command.ResendRegistrationOtpCommand;
import com.ecommerce.identity.application.dto.registration.command.VerifyRegistrationOtpCommand;
import com.ecommerce.identity.application.dto.registration.result.CompleteRegistrationResult;
import com.ecommerce.identity.application.dto.registration.result.InitiateRegistrationResult;
import com.ecommerce.identity.application.dto.registration.result.ResendRegistrationOtpResult;
import com.ecommerce.identity.application.dto.registration.result.VerifyRegistrationOtpResult;
import com.ecommerce.identity.application.mapper.RegistrationResultMapper;
import com.ecommerce.identity.application.port.out.auth.AccessTokenIssuer;
import com.ecommerce.identity.application.port.out.registration.CaptchaTokenValidator;
import com.ecommerce.identity.application.port.out.registration.RegistrationOtpSender;
import com.ecommerce.identity.application.port.out.registration.RegistrationPolicy;
import com.ecommerce.identity.application.port.out.registration.RegistrationRateLimiter;
import com.ecommerce.identity.application.port.out.registration.RegistrationSessionStore;
import com.ecommerce.identity.application.port.out.registration.RegistrationLockManager;
import com.ecommerce.identity.application.port.out.security.PasswordHasher;
import com.ecommerce.identity.domain.id.UserId;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.application.port.out.user.UserPersistencePort;
import com.ecommerce.identity.application.port.out.auth.AuthSessionPersistencePort;
import com.ecommerce.identity.domain.model.AuthSession;
import com.ecommerce.identity.domain.model.IdentityUser;
import com.ecommerce.identity.domain.model.RegistrationVerificationSession;
import com.ecommerce.identity.domain.valueobject.RegistrationOtp;
import com.ecommerce.sharedkernel.domain.vo.PhoneNumber;
import com.ecommerce.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service responsible for managing user registration flows.
 * 
 * <p>
 * This service orchestrates the multi-step registration process including:
 * </p>
 * <ul>
 * <li>Initiating registration with phone number validation and OTP
 * generation</li>
 * <li>Verifying OTP codes with attempt tracking</li>
 * <li>Resending OTP codes with cooldown enforcement</li>
 * <li>Completing registration by creating user accounts and auth sessions</li>
 * </ul>
 * 
 * <p>
 * The service enforces rate limiting, captcha validation, and distributed
 * locking
 * to prevent abuse and race conditions.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserPersistencePort userPersistencePort;
    private final AuthSessionPersistencePort authSessionPersistencePort;
    private final RegistrationOtpSender otpSender;
    private final CaptchaTokenValidator captchaTokenValidator;
    private final RegistrationRateLimiter registrationRateLimiter;
    private final RegistrationSessionStore registrationSessionStore;
    private final AccessTokenIssuer accessTokenIssuer;
    private final PasswordHasher passwordHasher;
    private final RegistrationResultMapper registrationResultMapper;
    private final RegistrationPolicy registrationPolicy;
    private final RegistrationLockManager registrationLockManager;

    /**
     * Initiates a new registration by validating the phone number, generating an
     * OTP,
     * and creating a verification session.
     * 
     * @param command the registration initiation command containing phone number
     *                and captcha
     * @return the result containing registration ID and OTP details
     * @throws IdentityException if captcha is invalid, phone is invalid/exists, or
     *                           rate limit exceeded
     */
    public InitiateRegistrationResult initiate(InitiateRegistrationCommand command) {
        log.debug("Initiating registration for identity: {}", command.identity());

        if (!captchaTokenValidator.isValid(command.captchaToken())) {
            log.warn("Invalid captcha token for registration from IP: {}", command.ipAddress());
            throw new IdentityException(IdentityErrorCode.CAPTCHA_INVALID);
        }

        PhoneNumber phoneNumber = validateAndNormalizePhoneNumber(command.identity());

        if (userPersistencePort.existsByPhone(phoneNumber.value())) {
            log.warn("Registration attempted with existing phone number: {}", phoneNumber.value());
            throw new IdentityException(IdentityErrorCode.PHONE_ALREADY_EXISTS);
        }

        enforceRateLimits(command.ipAddress(), phoneNumber.value());

        String regId = IdGenerator.ulid();
        RegistrationOtp otp = RegistrationOtp.generate(
                registrationPolicy.getResendCooldownSeconds(),
                registrationPolicy.getOtpExpirySeconds());

        var session = new RegistrationVerificationSession(
                regId,
                phoneNumber.value(),
                otp.getCode(),
                0,
                registrationPolicy.getMaxVerifyAttempts(),
                otp.getResendAvailableAt(),
                otp.getExpiredAt(),
                false,
                null,
                null);

        registrationSessionStore.save(session);
        otpSender.sendOtp(phoneNumber.value(), otp.getCode());

        log.info("Registration initiated successfully for phone: {}, regId: {}", phoneNumber.value(), regId);

        String responseOtpCode = registrationPolicy.isExposeOtpInResponse() ? otp.getCode() : null;

        return registrationResultMapper.toInitiateResult(session, responseOtpCode);
    }

    /**
     * Verifies the OTP code for a registration session.
     * 
     * @param command the verification command containing registration ID and OTP
     *                code
     * @return the result containing verification token
     * @throws IdentityException if session not found, OTP expired, invalid, or
     *                           attempts exceeded
     */
    public VerifyRegistrationOtpResult verifyOtp(VerifyRegistrationOtpCommand command) {
        log.debug("Verifying OTP for regId: {}", command.regId());

        var session = registrationSessionStore.findByRegId(command.regId())
                .orElseThrow(() -> {
                    log.warn("Registration session not found for regId: {}", command.regId());
                    return new IdentityException(IdentityErrorCode.REGISTRATION_NOT_FOUND);
                });

        try {
            session.verify(command.otpCode());
            log.info("OTP verified successfully for regId: {}", command.regId());
        } catch (IdentityException e) {
            log.warn("OTP verification failed for regId: {}, error: {}", command.regId(), e.getErrorCode());
            throw e;
        } finally {
            registrationSessionStore.save(session);
        }

        return registrationResultMapper.toVerifyOtpResult(session.getRegId(), session.getVerificationToken());
    }

    /**
     * Resends a new OTP code for an existing registration session.
     * 
     * @param command the resend command containing registration ID
     * @return the result containing new OTP details
     * @throws IdentityException if session not found, already verified, expired,
     *                           locked, or resent too soon
     */
    public ResendRegistrationOtpResult resendOtp(ResendRegistrationOtpCommand command) {
        log.debug("Resending OTP for regId: {}", command.regId());

        var session = registrationSessionStore.findByRegId(command.regId())
                .orElseThrow(() -> {
                    log.warn("Registration session not found for regId: {}", command.regId());
                    return new IdentityException(IdentityErrorCode.REGISTRATION_SESSION_NOT_FOUND);
                });

        RegistrationOtp newOtp = RegistrationOtp.generate(
                registrationPolicy.getResendCooldownSeconds(),
                registrationPolicy.getOtpExpirySeconds());

        session.resend(newOtp.getCode(), newOtp.getResendAvailableAt(), newOtp.getExpiredAt());
        registrationSessionStore.save(session);
        otpSender.sendOtp(session.getPhoneNumber(), newOtp.getCode());

        log.info("OTP resent successfully for regId: {}, phone: {}", command.regId(), session.getPhoneNumber());

        String responseOtpCode = registrationPolicy.isExposeOtpInResponse() ? newOtp.getCode() : null;

        return registrationResultMapper.toResendOtpResult(session, responseOtpCode);
    }

    /**
     * Completes the registration by creating a user account and auth session.
     * 
     * <p>
     * This method uses distributed locking to prevent race conditions when multiple
     * requests attempt to complete registration for the same phone number
     * simultaneously.
     * </p>
     * 
     * @param command the completion command containing registration ID, username,
     *                and password
     * @return the result containing auth session and access token
     * @throws IdentityException if session not found, expired, not verified, token
     *                           invalid,
     *                           phone/username already exists, or registration in
     *                           progress
     */
    public CompleteRegistrationResult complete(CompleteRegistrationCommand command) {
        log.debug("Completing registration for regId: {}", command.regId());

        var session = registrationSessionStore.findByRegId(command.regId())
                .orElseThrow(() -> {
                    log.warn("Registration session not found for regId: {}", command.regId());
                    return new IdentityException(IdentityErrorCode.REGISTRATION_NOT_FOUND);
                });

        String phoneNumber = session.getPhoneNumber();
        if (!registrationLockManager.tryLock(phoneNumber, 10)) {
            log.warn("Registration already in progress for phone: {}", phoneNumber);
            throw new IdentityException(IdentityErrorCode.REGISTRATION_IN_PROGRESS);
        }

        if (session.isExpired()) {
            log.warn("Registration session expired for regId: {}", command.regId());
            throw new IdentityException(IdentityErrorCode.REGISTRATION_EXPIRED);
        }

        if (!session.isVerified() || !command.verificationToken().equals(session.getVerificationToken())) {
            log.warn("Invalid verification token for regId: {}", command.regId());
            throw new IdentityException(IdentityErrorCode.VERIFICATION_TOKEN_INVALID);
        }

        try {
            validateUniqueIdentity(phoneNumber, command.username());

            IdentityUser user = createUser(phoneNumber, command.username(), command.password());
            IdentityUser savedUser = userPersistencePort.save(user);

            log.info("User created successfully: userId={}, username={}, phone={}",
                    savedUser.getId(), command.username(), phoneNumber);

            AuthSession authSession = createAuthSession(
                    savedUser.getId().toString(),
                    command.ipAddress(),
                    command.userAgent());
            AuthSession savedSession = authSessionPersistencePort.save(authSession);

            String accessToken = accessTokenIssuer.issueAccessToken(
                    savedUser.getId().toString(),
                    savedSession.getSessionId(),
                    savedSession.getExpiresAt());

            registrationSessionStore.deleteByRegId(command.regId());

            log.info("Registration completed successfully for userId: {}, sessionId: {}",
                    savedUser.getId(), savedSession.getSessionId());

            return registrationResultMapper.toCompleteResult(savedSession, accessToken);
        } finally {
            registrationLockManager.unlock(phoneNumber);
        }
    }

    /**
     * Validates that the phone number and username are unique in the system.
     * 
     * @param phoneNumber the phone number to validate
     * @param username    the username to validate
     * @throws IdentityException if phone or username already exists
     */
    private void validateUniqueIdentity(String phoneNumber, String username) {
        if (userPersistencePort.existsByPhone(phoneNumber)) {
            log.warn("Phone number already exists: {}", phoneNumber);
            throw new IdentityException(IdentityErrorCode.PHONE_ALREADY_EXISTS);
        }
        if (userPersistencePort.existsByUsername(username)) {
            log.warn("Username already exists: {}", username);
            throw new IdentityException(IdentityErrorCode.USERNAME_ALREADY_EXISTS);
        }
    }

    /**
     * Validates and normalizes a phone number to E.164 format.
     * 
     * @param identity the phone number string to validate
     * @return the normalized PhoneNumber value object
     * @throws IdentityException if the phone number is invalid
     */
    private PhoneNumber validateAndNormalizePhoneNumber(String identity) {
        try {
            PhoneNumber phoneNumber = PhoneNumber.of(identity);
            if (!phoneNumber.isE164()) {
                phoneNumber = PhoneNumber.of(phoneNumber.toE164(
                        registrationPolicy.getDefaultCountryCallingCode()));
            }
            return phoneNumber;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid phone number format: {}", identity);
            throw new IdentityException(IdentityErrorCode.PHONE_INVALID);
        }
    }

    /**
     * Enforces rate limits for registration attempts by IP address and phone
     * number.
     * 
     * @param ipAddress   the IP address to check
     * @param phoneNumber the phone number to check
     * @throws IdentityException if rate limit is exceeded
     */
    private void enforceRateLimits(String ipAddress, String phoneNumber) {
        if (!registrationRateLimiter.check("IP", ipAddress,
                registrationPolicy.getIpRateLimitMaxAttempts(),
                registrationPolicy.getIpRateLimitWindowSeconds())
                || !registrationRateLimiter.check("PHONE", phoneNumber,
                        registrationPolicy.getPhoneRateLimitMaxAttempts(),
                        registrationPolicy.getPhoneRateLimitWindowSeconds())) {
            log.warn("Rate limit exceeded for IP: {} or phone: {}", ipAddress, phoneNumber);
            throw new IdentityException(IdentityErrorCode.RATE_LIMIT_EXCEEDED);
        }
    }

    /**
     * Creates a new user with the provided credentials.
     * 
     * @param phoneNumber the user's phone number
     * @param username    the user's username
     * @param password    the user's password (will be hashed)
     * @return the created IdentityUser
     */
    private IdentityUser createUser(String phoneNumber, String username, String password) {
        IdentityUser user = IdentityUser.createNew(
                UserId.of(IdGenerator.ulid()),
                null,
                phoneNumber,
                username);
        user.updatePasswordHash(passwordHasher.hash(password));
        user.updateDisplayName(username);
        user.verifyPhone();
        return user;
    }

    /**
     * Creates a new authentication session for the user.
     * 
     * @param userId    the user's ID
     * @param ipAddress the IP address of the request
     * @param userAgent the user agent string
     * @return the created AuthSession
     */
    private AuthSession createAuthSession(String userId, String ipAddress, String userAgent) {
        return AuthSession.createNew(
                IdGenerator.ulid(),
                userId,
                ipAddress,
                userAgent,
                LocalDateTime.now().plusDays(registrationPolicy.getSessionExpiresDays()));
    }
}
