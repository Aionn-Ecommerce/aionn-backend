package com.aionn.identity.application.port.out.auth;

public interface AuthPolicy {

    long getSessionExpiresDays();

    int getAccessTokenExpiryMinutes();

    int getMaxFailedLoginAttempts();

    int getLockoutMinutes();
}
