package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.dto.registration.CompleteRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.CompleteRegistrationResult;
import com.ecommerce.identity.application.dto.registration.InitiateRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.InitiateRegistrationResult;
import com.ecommerce.identity.application.dto.registration.VerifyRegistrationOtpCommand;
import com.ecommerce.identity.application.dto.registration.VerifyRegistrationOtpResult;
import com.ecommerce.identity.application.port.out.auth.AccessTokenIssuer;
import com.ecommerce.identity.application.port.out.registration.CaptchaTokenValidator;
import com.ecommerce.identity.application.port.out.registration.RegistrationOtpSender;
import com.ecommerce.identity.application.port.out.registration.RegistrationPolicy;
import com.ecommerce.identity.application.port.out.registration.RegistrationRateLimiter;
import com.ecommerce.identity.application.port.out.registration.RegistrationSessionStore;
import com.ecommerce.identity.application.port.out.security.PasswordHasher;
import com.ecommerce.identity.domain.id.UserId;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.model.AuthSession;
import com.ecommerce.identity.domain.model.IdentityUser;
import com.ecommerce.identity.infrastructure.persistence.entity.AuthSessionEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import com.ecommerce.identity.infrastructure.persistence.mapper.AuthSessionDomainMapper;
import com.ecommerce.identity.infrastructure.persistence.mapper.IdentityUserMapper;
import com.ecommerce.identity.infrastructure.persistence.repository.auth.AuthSessionRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
import com.ecommerce.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private static final long SESSION_EXPIRES_DAYS = 30;
    private static final Pattern LOCAL_VN_PHONE_PATTERN = Pattern.compile("^0\\d{9}$");
    private static final Pattern E164_PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    private final UserRepository userRepository;
    private final RegistrationOtpSender otpSender;
    private final CaptchaTokenValidator captchaTokenValidator;
    private final RegistrationRateLimiter registrationRateLimiter;
    private final RegistrationSessionStore registrationSessionStore;
    private final RegistrationPolicy registrationPolicy;
    private final AuthSessionRepository authSessionRepository;
    private final AccessTokenIssuer accessTokenIssuer;
    private final PasswordHasher passwordHasher;
    private final IdentityUserMapper identityUserMapper;
    private final AuthSessionDomainMapper authSessionDomainMapper;

    @Transactional
    public InitiateRegistrationResult initiate(InitiateRegistrationCommand command) {
        if (!captchaTokenValidator.isValid(command.captchaToken())) {
            throw new IdentityException(IdentityErrorCode.CAPTCHA_INVALID);
        }

        String normalizedPhoneNumber = normalizePhoneNumber(command.identity());

        userRepository.findByPhone(normalizedPhoneNumber).ifPresent(existing -> {
            throw new IdentityException(IdentityErrorCode.PHONE_ALREADY_EXISTS);
        });

        if (!registrationRateLimiter.check("IP", command.ipAddress(), 3, 300)
                || !registrationRateLimiter.check("PHONE", normalizedPhoneNumber, 1, 60)) {
            throw new IdentityException(IdentityErrorCode.RATE_LIMIT_EXCEEDED);
        }

        String regId = IdGenerator.ulid();
        String otpCode = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resendAvailableAt = now.plusSeconds(registrationPolicy.resendCooldownSeconds());
        LocalDateTime expiredAt = now.plusSeconds(registrationPolicy.otpExpirySeconds());

        var session = new com.ecommerce.identity.domain.model.RegistrationVerificationSession(
                regId,
                normalizedPhoneNumber,
                otpCode,
                0,
                registrationPolicy.maxVerifyAttempts(),
                resendAvailableAt,
                expiredAt,
                false,
                null,
                null);

        registrationSessionStore.save(session);
        otpSender.sendOtp(normalizedPhoneNumber, otpCode);

        String responseOtpCode = registrationPolicy.exposeOtpInResponse() ? otpCode : null;

        return new InitiateRegistrationResult(regId, resendAvailableAt, expiredAt, responseOtpCode);
    }

    @Transactional
    public VerifyRegistrationOtpResult verifyOtp(VerifyRegistrationOtpCommand command) {
        var session = registrationSessionStore.findByRegId(command.regId())
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.REGISTRATION_NOT_FOUND));

        try {
            session.verify(command.otpCode());
        } finally {
            registrationSessionStore.save(session);
        }

        return new VerifyRegistrationOtpResult(session.getRegId(), session.getVerificationToken());
    }

    @Transactional
    public CompleteRegistrationResult complete(CompleteRegistrationCommand command) {
        var session = registrationSessionStore.findByRegId(command.regId())
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.REGISTRATION_NOT_FOUND));

        if (session.isExpired()) {
            throw new IdentityException(IdentityErrorCode.REGISTRATION_EXPIRED);
        }

        if (!session.isVerified() || !command.verificationToken().equals(session.getVerificationToken())) {
            throw new IdentityException(IdentityErrorCode.VERIFICATION_TOKEN_INVALID);
        }

        IdentityUser domainUser = IdentityUser.createNew(
                UserId.of(IdGenerator.ulid()),
                null,
                session.getPhoneNumber(),
                command.username());
        domainUser.updatePasswordHash(passwordHasher.hash(command.password()));
        domainUser.updateDisplayName(command.username());
        domainUser.verifyPhone();

        UserEntity savedUser = userRepository.save(identityUserMapper.toEntity(domainUser));

        AuthSession domainSession = AuthSession.createNew(
                IdGenerator.ulid(),
                savedUser.getUserId(),
                "registration",
                "registration-complete",
                LocalDateTime.now().plusDays(SESSION_EXPIRES_DAYS));

        AuthSessionEntity savedSession = authSessionRepository.save(
                authSessionDomainMapper.toEntity(domainSession, savedUser));
        String accessToken = accessTokenIssuer.issueAccessToken(
                savedUser.getUserId(),
                savedSession.getSessionId(),
                savedSession.getExpiresAt());

        registrationSessionStore.deleteByRegId(command.regId());

        return new CompleteRegistrationResult(
                savedUser.getUserId(),
                savedSession.getSessionId(),
                accessToken,
                savedSession.getExpiresAt());
    }

    private String normalizePhoneNumber(String phoneNumber) {
        String normalized = phoneNumber == null ? "" : phoneNumber.trim();
        if (E164_PHONE_PATTERN.matcher(normalized).matches()) {
            return normalized;
        }
        if (LOCAL_VN_PHONE_PATTERN.matcher(normalized).matches()) {
            return "+84" + normalized.substring(1);
        }
        return normalized;
    }
}
