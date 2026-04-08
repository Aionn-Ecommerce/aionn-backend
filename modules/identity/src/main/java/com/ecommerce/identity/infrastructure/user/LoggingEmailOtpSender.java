package com.ecommerce.identity.infrastructure.user;

import com.ecommerce.identity.application.port.out.user.EmailOtpSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggingEmailOtpSender implements EmailOtpSender {

    @Override
    public void sendOtp(String email, String otpCode) {
        log.info("[USER-EMAIL-OTP] email={}, otp={}", email, otpCode);
    }
}


