package com.ecommerce.identity.infrastructure.registration;

import com.ecommerce.identity.application.port.out.registration.RegistrationSessionStore;
import com.ecommerce.identity.domain.model.RegistrationVerificationSession;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RedisRegistrationSessionStore implements RegistrationSessionStore {

    private final RegistrationSessionRedisManager registrationSessionRedisManager;

    public RedisRegistrationSessionStore(
            RegistrationSessionRedisManager registrationSessionRedisManager) {
        this.registrationSessionRedisManager = registrationSessionRedisManager;
    }

    @Override
    public void save(RegistrationVerificationSession session) {
        registrationSessionRedisManager.save(session);
    }

    @Override
    public Optional<RegistrationVerificationSession> findByRegId(String regId) {
        return registrationSessionRedisManager.findByRegId(regId);
    }

    @Override
    public void deleteByRegId(String regId) {
        registrationSessionRedisManager.deleteByRegId(regId);
    }
}


