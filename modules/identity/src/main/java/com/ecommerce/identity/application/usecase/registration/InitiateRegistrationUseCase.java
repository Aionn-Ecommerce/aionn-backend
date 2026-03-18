package com.ecommerce.identity.application.usecase.registration;

import com.ecommerce.identity.application.dto.registration.InitiateRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.InitiateRegistrationResult;
import com.ecommerce.identity.application.port.in.registration.InitiateRegistrationInputPort;
import com.ecommerce.identity.application.port.out.registration.CaptchaTokenValidator;
import com.ecommerce.identity.application.port.out.registration.RegistrationOtpSender;
import com.ecommerce.identity.application.port.out.registration.RegistrationRateLimiter;
import com.ecommerce.identity.application.port.out.registration.RegistrationSessionStore;
import com.ecommerce.identity.application.port.out.registration.model.RegistrationVerificationSession;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.repository.IdentityUserRepository;
import com.ecommerce.identity.infrastructure.config.IdentityRegistrationProperties;
import com.ecommerce.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class InitiateRegistrationUseCase implements InitiateRegistrationInputPort {

    private static final Pattern LOCAL_VN_PHONE_PATTERN = Pattern.compile("^0\\d{9}$");
    private static final Pattern E164_PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    private final IdentityUserRepository identityUserRepository;
    private final RegistrationOtpSender otpSender;
    private final CaptchaTokenValidator captchaTokenValidator;
    private final RegistrationRateLimiter registrationRateLimiter;
    private final RegistrationSessionStore registrationSessionStore;
    private final IdentityRegistrationProperties registrationProperties;

    @Override
    public InitiateRegistrationResult execute(InitiateRegistrationCommand command) {
        if (!captchaTokenValidator.isValid(command.captchaToken())) {
            throw new IdentityException(IdentityErrorCode.CAPTCHA_INVALID);
        }

        String normalizedPhoneNumber = normalizePhoneNumber(command.identity());

        identityUserRepository.findByPhone(normalizedPhoneNumber).ifPresent(existing -> {
            throw new IdentityException(IdentityErrorCode.PHONE_ALREADY_EXISTS);
        });

        if (!registrationRateLimiter.check("IP", command.ipAddress(), 3, 300)
                || !registrationRateLimiter.check("PHONE", normalizedPhoneNumber, 1, 60)) {
            throw new IdentityException(IdentityErrorCode.RATE_LIMIT_EXCEEDED);
        }

        String regId = IdGenerator.ulid();
        String otpCode = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resendAvailableAt = now.plusSeconds(registrationProperties.getResendCooldownSeconds());
        LocalDateTime expiredAt = now.plusSeconds(registrationProperties.getOtpExpirySeconds());

        RegistrationVerificationSession session = new RegistrationVerificationSession(
                regId,
                normalizedPhoneNumber,
                otpCode,
                0,
                registrationProperties.getMaxVerifyAttempts(),
                resendAvailableAt,
                expiredAt,
                false,
                null,
                null);

        registrationSessionStore.save(session);
        otpSender.sendOtp(normalizedPhoneNumber, otpCode);

        String responseOtpCode = registrationProperties.isExposeOtpInResponse() ? otpCode : null;

        return new InitiateRegistrationResult(regId, resendAvailableAt, expiredAt, responseOtpCode);
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
