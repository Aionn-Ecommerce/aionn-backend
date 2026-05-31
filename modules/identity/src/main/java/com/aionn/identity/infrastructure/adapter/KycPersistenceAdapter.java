package com.aionn.identity.infrastructure.adapter;

import com.aionn.identity.application.port.out.kyc.KycPersistencePort;
import com.aionn.identity.domain.model.KycProfile;
import com.aionn.identity.infrastructure.persistence.entity.KycProfileEntity;
import com.aionn.identity.infrastructure.persistence.entity.UserEntity;
import com.aionn.identity.infrastructure.persistence.mapper.KycDomainMapper;
import com.aionn.identity.infrastructure.persistence.repository.kyc.KycProfileRepository;
import com.aionn.identity.infrastructure.persistence.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class KycPersistenceAdapter implements KycPersistencePort {

    private final KycProfileRepository kycRepository;
    private final UserRepository userRepository;
    private final KycDomainMapper mapper;

    @Override
    public KycProfile save(KycProfile kycProfile) {
        KycProfileEntity entity = mapper.toEntity(kycProfile);
        UserEntity userRef = userRepository.getReferenceById(kycProfile.getUserId());
        entity.setUser(userRef);
        KycProfileEntity saved = kycRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<KycProfile> findByKycIdAndUserId(String kycId, String userId) {
        return kycRepository.findByKycIdAndUser_UserId(kycId, userId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<KycProfile> findById(String kycId) {
        return kycRepository.findById(kycId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<KycProfile> findByProviderApplicantId(String providerApplicantId) {
        return kycRepository.findByProviderApplicantId(providerApplicantId)
                .map(mapper::toDomain);
    }

    @Override
    public List<KycProfile> findByUserIdOrderBySubmittedAtDesc(String userId) {
        return kycRepository.findByUser_UserIdOrderBySubmittedAtDesc(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(KycProfile kycProfile) {
        kycRepository.deleteById(kycProfile.getKycId());
    }
}
