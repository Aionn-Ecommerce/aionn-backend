package com.ecommerce.identity.application.port.out.user;

public interface PhoneOtpSender {

    void sendOtp(String phoneNumber, String otpCode);
}


