package com.aionn.identity.application.port.out.security;

public interface TotpManager {

    String generateSecret();

    boolean verifyCode(String secret, String code);

    String buildOtpAuthUri(String issuer, String accountName, String secret);
}
