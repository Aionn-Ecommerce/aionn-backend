package com.ecommerce.identity.infrastructure.adapter;

import com.ecommerce.identity.application.port.out.kyc.KycPersistencePort;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.model.KycProfile;
import com.ecommerce.identity.infrastructure.persistence.entity.KycProfileEntity;
import com.ecommerce.identity.infrastructure.persistence.mapper.KycDomainMapper;
import com.ecommerce.identity.infrastructure.persistence.repository.kyc.KycProfileRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
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
        var user = userRepository.findById(kycProfile.getUserId())
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));

        KycProfileEntity entity = mapper.toEntity(kycProfile);
        entity.setUser(user);

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
