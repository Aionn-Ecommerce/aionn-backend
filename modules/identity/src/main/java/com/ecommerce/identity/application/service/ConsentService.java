package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.mapper.ConsentResultMapper;
import com.ecommerce.identity.application.port.out.consent.ConsentPersistencePort;
import com.ecommerce.identity.application.port.out.user.UserPersistencePort;
import com.ecommerce.identity.application.dto.consent.result.ConsentResult;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.model.UserConsent;
import com.ecommerce.identity.domain.valueobject.ConsentType;
import com.ecommerce.identity.infrastructure.persistence.mapper.UserConsentDomainMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing user consents.
 * Handles recording and retrieving user agreements to terms, privacy policies,
 * and marketing communications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentService {

    private final UserPersistencePort userPersistencePort;
    private final ConsentPersistencePort consentPersistencePort;
    private final ConsentResultMapper consentResultMapper;
    private final UserConsentDomainMapper userConsentDomainMapper;

    /**
     * Records user agreement to terms and conditions.
     *
     * @param userId    the user ID
     * @param version   the version of terms agreed to
     * @param ipAddress the IP address from which consent was given
     * @return the consent result
     * @throws IdentityException if user not found or IP address is invalid
     */
    public ConsentResult agreeTerms(String userId, String version, String ipAddress) {
        log.info("Recording terms consent for user: {}, version: {}", userId, version);
        return saveConsent(userId, ConsentType.TERMS, version, ipAddress, true);
    }

    /**
     * Records user agreement to privacy policy.
     *
     * @param userId    the user ID
     * @param version   the version of privacy policy agreed to
     * @param ipAddress the IP address from which consent was given
     * @return the consent result
     * @throws IdentityException if user not found or IP address is invalid
     */
    public ConsentResult agreePrivacy(String userId, String version, String ipAddress) {
        log.info("Recording privacy consent for user: {}, version: {}", userId, version);
        return saveConsent(userId, ConsentType.PRIVACY, version, ipAddress, true);
    }

    /**
     * Updates user marketing consent preference.
     *
     * @param userId     the user ID
     * @param subscribed whether user subscribes to marketing
     * @param ipAddress  the IP address from which consent was given
     * @return the consent result
     * @throws IdentityException if user not found or IP address is invalid
     */
    public ConsentResult updateMarketing(String userId, boolean subscribed, String ipAddress) {
        log.info("Updating marketing consent for user: {}, subscribed: {}", userId, subscribed);
        return saveConsent(userId, ConsentType.MARKETING, "v1", ipAddress, subscribed);
    }

    /**
     * Lists all consents for a user.
     *
     * @param userId the user ID
     * @return list of consent results
     * @throws IdentityException if user not found
     */
    public List<ConsentResult> listMy(String userId) {
        log.debug("Listing consents for user: {}", userId);
        userPersistencePort.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
        var entities = consentPersistencePort.findByUserIdOrderByAgreedAtDesc(userId);
        return consentResultMapper.toResults(entities);
    }

    /**
     * Saves or updates a consent record.
     *
     * @param userId      the user ID
     * @param consentType the type of consent
     * @param version     the version of the consent document
     * @param ipAddress   the IP address from which consent was given
     * @param agreed      whether the user agreed or revoked consent
     * @return the consent result
     * @throws IdentityException if user not found or IP address is invalid
     */
    private ConsentResult saveConsent(
            String userId,
            ConsentType consentType,
            String version,
            String ipAddress,
            boolean agreed) {

        // Validate IP address format
        if (!UserConsent.isValidIpAddress(ipAddress)) {
            log.warn("Invalid IP address format provided: {}", ipAddress);
            throw new IdentityException(IdentityErrorCode.INVALID_IP_ADDRESS);
        }

        // Verify user exists
        userPersistencePort.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));

        // Find or create consent record
        var consentEntity = consentPersistencePort
                .findLatestByUserIdAndType(userId, consentType.name())
                .orElseGet(
                        () -> consentPersistencePort.createNewConsent(userId, consentType.name(), version, ipAddress));

        // Map to domain model
        var consent = userConsentDomainMapper.toDomain(consentEntity);

        // Update consent state using domain logic
        if (agreed) {
            consent.grant();
        } else {
            consent.revoke();
        }

        // Map back to entity and save
        var updatedEntity = userConsentDomainMapper.toEntity(consent);
        updatedEntity.setVersion(version);
        updatedEntity.setIpAddress(ipAddress);
        updatedEntity.setUser(consentEntity.getUser()); // Preserve user reference
        updatedEntity.setConsentId(consentEntity.getConsentId()); // Preserve ID
        updatedEntity.setAgreedAt(consentEntity.getAgreedAt()); // Preserve original agreed time

        var saved = consentPersistencePort.save(updatedEntity);

        log.info("Consent saved for user: {}, type: {}, agreed: {}", userId, consentType, agreed);
        return consentResultMapper.toResult(saved);
    }
}
