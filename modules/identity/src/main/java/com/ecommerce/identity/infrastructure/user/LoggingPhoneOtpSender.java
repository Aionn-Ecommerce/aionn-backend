package com.ecommerce.identity.infrastructure.user;

import com.ecommerce.identity.application.port.out.user.PhoneOtpSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggingPhoneOtpSender implements PhoneOtpSender {

    @Override
    public void sendOtp(String phoneNumber, String otpCode) {
        log.info("[USER-PHONE-OTP] phone={}, otp={}", phoneNumber, otpCode);
    }
}


