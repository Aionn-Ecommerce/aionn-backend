package com.aionn.identity.application.port.out.auth;

public interface TokenBlacklist {

    void blacklist(String jti, long ttlSeconds);

    boolean isBlacklisted(String jti);
}
