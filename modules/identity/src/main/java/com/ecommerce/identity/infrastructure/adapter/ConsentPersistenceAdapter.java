package com.ecommerce.identity.infrastructure.adapter;

import com.ecommerce.identity.application.port.out.consent.ConsentPersistencePort;
import com.ecommerce.identity.infrastructure.persistence.entity.UserConsentEntity;
import com.ecommerce.identity.infrastructure.persistence.repository.consent.UserConsentRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
import com.ecommerce.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ConsentPersistenceAdapter implements ConsentPersistencePort {

    private final UserConsentRepository consentRepository;
    private final UserRepository userRepository;

    @Override
    public UserConsentEntity save(UserConsentEntity consent) {
        return consentRepository.save(consent);
    }

    @Override
    public Optional<UserConsentEntity> findLatestByUserIdAndType(String userId, String consentType) {
        return consentRepository.findTopByUser_UserIdAndConsentTypeOrderByAgreedAtDesc(userId, consentType);
    }

    @Override
    public List<UserConsentEntity> findByUserIdOrderByAgreedAtDesc(String userId) {
        return consentRepository.findByUser_UserIdOrderByAgreedAtDesc(userId);
    }

    @Override
    public UserConsentEntity createNewConsent(String userId, String consentType, String version, String ipAddress) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return UserConsentEntity.builder()
                .consentId(IdGenerator.ulid())
                .user(user)
                .consentType(consentType)
                .version(version)
                .ipAddress(ipAddress)
                .build();
    }
}
