package com.aionn.identity.infrastructure.user;

import com.aionn.identity.application.port.out.user.PhoneOtpSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "identity.user.phone-otp", name = "provider", havingValue = "logging", matchIfMissing = true)
public class LoggingPhoneOtpSender implements PhoneOtpSender {

    @Override
    public void sendOtp(String phoneNumber, String otpCode) {
        log.info("[USER-PHONE-OTP] phone={} (otp redacted)", phoneNumber);
        log.debug("[USER-PHONE-OTP] phone={} otp={}", phoneNumber, otpCode);
    }
}

