package com.ecommerce.identity.infrastructure.notification;

import com.ecommerce.identity.application.port.out.registration.RegistrationOtpSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "identity.registration.twilio", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingRegistrationOtpSender implements RegistrationOtpSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingRegistrationOtpSender.class);

    @Override
    public void sendOtp(String phoneNumber, String otpCode) {
        log.info("[IDENTITY-OTP] Sending OTP to {}: {}", phoneNumber, otpCode);
    }
}
