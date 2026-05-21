package com.aionn.identity.infrastructure.user;

import com.aionn.identity.application.port.out.user.EmailOtpSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Development OTP sender - logs the code instead of dispatching email. Use the
 * remote sender (to be implemented) once an SMTP/email provider is chosen.
 *
 * <p>
 * Activates only when {@code identity.user.email-otp.provider=logging}, the
 * default for local profiles.
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "identity.user.email-otp", name = "provider", havingValue = "logging", matchIfMissing = true)
public class LoggingEmailOtpSender implements EmailOtpSender {

    @Override
    public void sendOtp(String email, String otpCode) {
        log.info("[USER-EMAIL-OTP] email={} (otp redacted)", email);
        log.debug("[USER-EMAIL-OTP] email={} otp={}", email, otpCode);
    }
}

