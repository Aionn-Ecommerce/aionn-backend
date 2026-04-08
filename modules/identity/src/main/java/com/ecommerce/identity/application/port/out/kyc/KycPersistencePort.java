package com.ecommerce.identity.application.port.out.kyc;

import com.ecommerce.identity.domain.model.KycProfile;

import java.util.List;
import java.util.Optional;

public interface KycPersistencePort {
    KycProfile save(KycProfile kycProfile);

    Optional<KycProfile> findByKycIdAndUserId(String kycId, String userId);

    Optional<KycProfile> findById(String kycId);

    List<KycProfile> findByUserIdOrderBySubmittedAtDesc(String userId);

    void delete(KycProfile kycProfile);
}
