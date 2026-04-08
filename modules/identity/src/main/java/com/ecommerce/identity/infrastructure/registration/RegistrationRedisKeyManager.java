package com.ecommerce.identity.infrastructure.registration;

import org.springframework.stereotype.Component;

@Component
public class RegistrationRedisKeyManager {

    private static final String REGISTRATION_SESSION_PREFIX = "identity:registration:session:";

    public String registrationSessionKey(String regId) {
        return REGISTRATION_SESSION_PREFIX + regId;
    }
}


