package com.ecommerce.identity.application.port.out.registration;

public interface RegistrationOtpSender {
    void sendOtp(String phoneNumber, String otpCode);
}


