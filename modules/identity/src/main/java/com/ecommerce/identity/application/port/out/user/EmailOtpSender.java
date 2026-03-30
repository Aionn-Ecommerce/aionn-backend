package com.ecommerce.identity.application.port.out.user;

public interface EmailOtpSender {

    void sendOtp(String email, String otpCode);
}
