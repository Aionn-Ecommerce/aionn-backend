package com.ecommerce.identity.application.port.out.consent;

import com.ecommerce.identity.infrastructure.persistence.entity.UserConsentEntity;

import java.util.List;
import java.util.Optional;

public interface ConsentPersistencePort {
    UserConsentEntity save(UserConsentEntity consent);

    Optional<UserConsentEntity> findLatestByUserIdAndType(String userId, String consentType);

    List<UserConsentEntity> findByUserIdOrderByAgreedAtDesc(String userId);

    UserConsentEntity createNewConsent(String userId, String consentType, String version, String ipAddress);
}
