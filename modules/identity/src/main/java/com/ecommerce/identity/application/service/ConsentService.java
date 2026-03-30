package com.ecommerce.identity.application.service;

import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.infrastructure.persistence.entity.UserConsentEntity;
import com.ecommerce.identity.infrastructure.persistence.repository.consent.UserConsentRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
import com.ecommerce.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsentService {

    private final UserRepository userRepository;
    private final UserConsentRepository consentRepository;

    @Transactional
    public UserConsentEntity agreeTerms(String userId, String version, String ipAddress) {
        return saveConsent(userId, "TERMS", version, ipAddress, true);
    }

    @Transactional
    public UserConsentEntity agreePrivacy(String userId, String version, String ipAddress) {
        return saveConsent(userId, "PRIVACY", version, ipAddress, true);
    }

    @Transactional
    public UserConsentEntity updateMarketing(String userId, boolean subscribed, String ipAddress) {
        return saveConsent(userId, "MARKETING", "v1", ipAddress, subscribed);
    }

    @Transactional(readOnly = true)
    public List<UserConsentEntity> listMy(String userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
        return consentRepository.findByUser_UserIdOrderByAgreedAtDesc(userId);
    }

    private UserConsentEntity saveConsent(
            String userId,
            String consentType,
            String version,
            String ipAddress,
            boolean agreed) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
        UserConsentEntity consent = consentRepository
                .findTopByUser_UserIdAndConsentTypeOrderByAgreedAtDesc(userId, consentType)
                .orElse(UserConsentEntity.builder()
                        .consentId(IdGenerator.ulid())
                        .user(user)
                        .consentType(consentType)
                        .version(version)
                        .build());
        consent.setVersion(version);
        consent.setIpAddress(ipAddress);
        consent.setRevokedAt(agreed ? null : LocalDateTime.now());
        return consentRepository.save(consent);
    }
}
