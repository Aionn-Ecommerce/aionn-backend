package com.aionn.identity.application.service;

import com.aionn.identity.application.port.out.kyc.KycPersistencePort;
import com.aionn.identity.application.port.out.user.UserPersistencePort;
import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.identity.domain.model.KycProfile;
import com.aionn.identity.domain.valueobject.KycStatus;
import com.aionn.identity.domain.valueobject.UserRole;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing KYC profiles.
 *
 * <h3>Business rules</h3>
 * <ul>
 * <li>State machine: DRAFT â†’ SUBMITTED â†’ IN_REVIEW â†’ APPROVED/REJECTED.
 * Cancel transitions to CANCELLED. Rejected applicants may restart from
 * DRAFT.</li>
 * <li>Cancel preserves history: we update {@link KycStatus#CANCELLED} instead
 * of deleting the row.</li>
 * <li>Admin operations (review/approve/reject) require {@code CS_ADMIN} or
 * {@code SYSTEM_ADMIN} role.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KycService {

    private final KycPersistencePort kycPersistencePort;
    private final UserPersistencePort userPersistencePort;

    public KycProfile createKyc(String userId, String docType) {
        log.info("Creating KYC profile for user: {}, docType: {}", userId, docType);
        validateUserExists(userId);

        KycProfile kyc = new KycProfile(
                IdGenerator.ulid(),
                userId,
                docType,
                null,
                KycStatus.DRAFT,
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.now());

        return kycPersistencePort.save(kyc);
    }

    public KycProfile uploadDocument(String userId, String kycId, String blobUrl) {
        log.info("Uploading document for KYC: {}, user: {}", kycId, userId);
        KycProfile kyc = getKycByUser(kycId, userId);
        kyc.uploadDocument(blobUrl);
        return kycPersistencePort.save(kyc);
    }

    public KycProfile submit(String userId, String kycId) {
        log.info("Submitting KYC: {}, user: {}", kycId, userId);
        KycProfile kyc = getKycByUser(kycId, userId);
        kyc.submit();
        return kycPersistencePort.save(kyc);
    }

    /**
     * Cancel a KYC profile by transitioning it to {@link KycStatus#CANCELLED}.
     * The record is preserved for audit.
     */
    public KycProfile cancel(String userId, String kycId) {
        log.info("Cancelling KYC: {}, user: {}", kycId, userId);
        KycProfile kyc = getKycByUser(kycId, userId);
        kyc.cancel();
        return kycPersistencePort.save(kyc);
    }

    public KycProfile review(String adminUserId, String kycId, String note) {
        log.info("Admin {} reviewing KYC: {}", adminUserId, kycId);
        validateAdminUser(adminUserId);
        KycProfile kyc = getKyc(kycId);
        kyc.review(adminUserId, note);
        return kycPersistencePort.save(kyc);
    }

    public KycProfile approve(String adminUserId, String kycId) {
        log.info("Admin {} approving KYC: {}", adminUserId, kycId);
        validateAdminUser(adminUserId);
        KycProfile kyc = getKyc(kycId);
        kyc.approve(adminUserId);
        return kycPersistencePort.save(kyc);
    }

    public KycProfile reject(String adminUserId, String kycId, String reason) {
        log.info("Admin {} rejecting KYC: {}", adminUserId, kycId);
        validateAdminUser(adminUserId);
        KycProfile kyc = getKyc(kycId);
        kyc.reject(adminUserId, reason);
        return kycPersistencePort.save(kyc);
    }

    public List<KycProfile> listMy(String userId) {
        log.debug("Listing KYC profiles for user: {}", userId);
        validateUserExists(userId);
        return kycPersistencePort.findByUserIdOrderBySubmittedAtDesc(userId);
    }

    public KycProfile get(String userId, String kycId) {
        log.debug("Getting KYC: {}, user: {}", kycId, userId);
        validateUserExists(userId);
        return getKycByUser(kycId, userId);
    }

    private KycProfile getKycByUser(String kycId, String userId) {
        return kycPersistencePort.findByKycIdAndUserId(kycId, userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.KYC_NOT_FOUND));
    }

    private KycProfile getKyc(String kycId) {
        return kycPersistencePort.findById(kycId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.KYC_NOT_FOUND));
    }

    private void validateUserExists(String userId) {
        userPersistencePort.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
    }

    private void validateAdminUser(String adminUserId) {
        var user = userPersistencePort.findById(adminUserId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));

        boolean isAdmin = user.getRoles().contains(UserRole.CS_ADMIN)
                || user.getRoles().contains(UserRole.SYSTEM_ADMIN);

        if (!isAdmin) {
            log.warn("User {} attempted admin KYC operation without admin role", adminUserId);
            throw new IdentityException(IdentityErrorCode.INSUFFICIENT_PERMISSIONS);
        }
    }
}

