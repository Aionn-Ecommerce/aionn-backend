package com.ecommerce.identity.application.port.out.registration;

import com.ecommerce.identity.application.port.out.registration.model.RegistrationVerificationSession;

import java.util.Optional;

public interface RegistrationSessionStore {

    void save(RegistrationVerificationSession session);

    Optional<RegistrationVerificationSession> findByRegId(String regId);

    void deleteByRegId(String regId);
}