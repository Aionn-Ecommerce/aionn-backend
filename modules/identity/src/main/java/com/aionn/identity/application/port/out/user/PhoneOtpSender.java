package com.aionn.identity.application.port.out.user;

public interface PhoneOtpSender {

    void sendOtp(String phoneNumber, String otpCode);
}



