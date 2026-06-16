package com.aionn.identity.infrastructure.persistence.repository.kyc;

import com.aionn.identity.infrastructure.persistence.entity.KycProfileEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KycProfileRepository extends JpaRepository<KycProfileEntity, String> {

    List<KycProfileEntity> findByUser_UserIdOrderBySubmittedAtDesc(String userId);

    Optional<KycProfileEntity> findByKycIdAndUser_UserId(String kycId, String userId);

    Optional<KycProfileEntity> findByProviderApplicantId(String providerApplicantId);

    List<KycProfileEntity> findByStatusOrderBySubmittedAtDesc(String status, Pageable pageable);
}
