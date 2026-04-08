package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.port.out.kyc.KycPersistencePort;
import com.ecommerce.identity.application.port.out.user.UserPersistencePort;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.model.KycProfile;
import com.ecommerce.identity.domain.valueobject.KycStatus;
import com.ecommerce.identity.domain.valueobject.UserRole;
import com.ecommerce.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing KYC (Know Your Customer) profiles.
 * Handles KYC profile creation, document upload, submission, and admin review
 * operations.
 * 
 * <p>
 * Business Rules:
 * <ul>
 * <li>KYC profiles follow a state machine: DRAFT → SUBMITTED → IN_REVIEW →
 * APPROVED/REJECTED</li>
 * <li>Only DRAFT and SUBMITTED profiles can be cancelled</li>
 * <li>Admin operations (review, approve, reject) require CS_ADMIN or
 * SYSTEM_ADMIN role</li>
 * <li>Status transitions are validated to ensure valid state machine flow</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KycService {

    private final KycPersistencePort kycPersistencePort;
    private final UserPersistencePort userPersistencePort;

    /**
     * Creates a new KYC profile for a user.
     * Initial status is DRAFT with blobUrl set to "PENDING_UPLOAD".
     *
     * @param userId  the user ID
     * @param docType the document type
     * @return the created KYC profile
     * @throws IdentityException if user not found
     */
    public KycProfile createKyc(String userId, String docType) {
        log.info("Creating KYC profile for user: {}, docType: {}", userId, docType);
        validateUserExists(userId);

        KycProfile kyc = new KycProfile(
                IdGenerator.ulid(),
                userId,
                docType,
                "PENDING_UPLOAD",
                KycStatus.DRAFT,
                null,
                null,
                null,
                null,
                LocalDateTime.now());

        return kycPersistencePort.save(kyc);
    }

    /**
     * Uploads a document for a KYC profile.
     *
     * @param userId  the user ID
     * @param kycId   the KYC profile ID
     * @param blobUrl the blob storage URL of the uploaded document
     * @return the updated KYC profile
     * @throws IdentityException if KYC profile not found
     */
    public KycProfile uploadDocument(String userId, String kycId, String blobUrl) {
        log.info("Uploading document for KYC: {}, user: {}", kycId, userId);
        KycProfile kyc = getKycByUser(kycId, userId);
        kyc.uploadDocument(blobUrl);
        return kycPersistencePort.save(kyc);
    }

    /**
     * Submits a KYC profile for review.
     * Validates status transition from DRAFT to SUBMITTED.
     *
     * @param userId the user ID
     * @param kycId  the KYC profile ID
     * @return the updated KYC profile
     * @throws IdentityException if KYC profile not found or invalid status
     *                           transition
     */
    public KycProfile submit(String userId, String kycId) {
        log.info("Submitting KYC: {}, user: {}", kycId, userId);
        KycProfile kyc = getKycByUser(kycId, userId);
        // Status transition validation is performed in kyc.submit()
        kyc.submit();
        return kycPersistencePort.save(kyc);
    }

    /**
     * Cancels a KYC profile.
     * Only DRAFT and SUBMITTED profiles can be cancelled.
     *
     * @param userId the user ID
     * @param kycId  the KYC profile ID
     * @throws IdentityException if KYC profile not found or cannot be cancelled
     */
    public void cancel(String userId, String kycId) {
        log.info("Cancelling KYC: {}, user: {}", kycId, userId);
        KycProfile kyc = getKycByUser(kycId, userId);

        if (!kyc.canBeCancelled()) {
            throw new IdentityException(IdentityErrorCode.KYC_CANNOT_BE_CANCELLED);
        }

        kycPersistencePort.delete(kyc);
    }

    /**
     * Admin operation to review a KYC profile.
     * Validates status transition to IN_REVIEW.
     *
     * @param adminUserId the admin user ID
     * @param kycId       the KYC profile ID
     * @param note        review notes
     * @return the updated KYC profile
     * @throws IdentityException if admin not authorized, KYC not found, or invalid
     *                           status transition
     */
    public KycProfile review(String adminUserId, String kycId, String note) {
        log.info("Admin {} reviewing KYC: {}", adminUserId, kycId);
        validateAdminUser(adminUserId);
        KycProfile kyc = getKyc(kycId);
        // Status transition validation is performed in kyc.review()
        kyc.review(adminUserId, note);
        return kycPersistencePort.save(kyc);
    }

    /**
     * Admin operation to approve a KYC profile.
     * Validates status transition to APPROVED.
     *
     * @param adminUserId the admin user ID
     * @param kycId       the KYC profile ID
     * @return the updated KYC profile
     * @throws IdentityException if admin not authorized, KYC not found, or invalid
     *                           status transition
     */
    public KycProfile approve(String adminUserId, String kycId) {
        log.info("Admin {} approving KYC: {}", adminUserId, kycId);
        validateAdminUser(adminUserId);
        KycProfile kyc = getKyc(kycId);
        // Status transition validation is performed in kyc.approve()
        kyc.approve(adminUserId);
        return kycPersistencePort.save(kyc);
    }

    /**
     * Admin operation to reject a KYC profile.
     * Validates status transition to REJECTED.
     *
     * @param adminUserId the admin user ID
     * @param kycId       the KYC profile ID
     * @param reason      rejection reason
     * @return the updated KYC profile
     * @throws IdentityException if admin not authorized, KYC not found, or invalid
     *                           status transition
     */
    public KycProfile reject(String adminUserId, String kycId, String reason) {
        log.info("Admin {} rejecting KYC: {}", adminUserId, kycId);
        validateAdminUser(adminUserId);
        KycProfile kyc = getKyc(kycId);
        // Status transition validation is performed in kyc.reject()
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

    /**
     * Validates that the user has admin privileges (CS_ADMIN or SYSTEM_ADMIN role).
     *
     * @param adminUserId the admin user ID
     * @throws IdentityException if user not found or lacks admin role
     */
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
