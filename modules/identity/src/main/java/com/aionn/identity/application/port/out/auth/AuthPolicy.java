package com.aionn.identity.application.port.out.auth;

public interface AuthPolicy {
    long getSessionExpiresDays();

    /**
     * Access token lifetime in minutes. Used to cap blacklist TTL.
     */
    int getAccessTokenExpiryMinutes();
}
