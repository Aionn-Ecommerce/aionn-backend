package com.aionn.identity.application.service;

import com.aionn.identity.application.dto.kyc.result.KycVerificationSessionResult;
import com.aionn.identity.application.policy.KycPolicy;
import com.aionn.identity.application.port.out.kyc.ExternalKycVerificationPort;
import com.aionn.identity.application.port.out.kyc.KycPersistencePort;
import com.aionn.identity.application.port.out.user.UserPersistencePort;
import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.identity.domain.model.IdentityUser;
import com.aionn.identity.domain.model.KycProfile;
import com.aionn.identity.domain.valueobject.KycReviewAnswer;
import com.aionn.identity.domain.valueobject.KycStatus;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KycService {

    private final KycPersistencePort kycPersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final KycPolicy kycPolicy;
    private final ExternalKycVerificationPort externalKycVerificationPort;

    public KycProfile createKyc(String userId, String docType) {
        log.info("Creating KYC profile for user: {}, docType: {}", userId, docType);
        IdentityUser user = requireUser(userId);
        boolean managedProviderEnabled = kycPolicy.usesManagedProvider();

        KycProfile kyc = new KycProfile(
                IdGenerator.ulid(),
                userId,
                docType,
                null,
                managedProviderEnabled ? KycStatus.SUBMITTED : KycStatus.DRAFT,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                managedProviderEnabled ? LocalDateTime.now() : null,
                null,
                LocalDateTime.now());

        if (!managedProviderEnabled) {
            return kycPersistencePort.save(kyc);
        }

        var applicant = externalKycVerificationPort.createApplicant(user, kyc.getKycId(), docType);
        kyc.attachExternalProvider(
                applicant.provider(),
                applicant.applicantId(),
                applicant.levelName(),
                applicant.reviewStatus(),
                applicant.correlationId());
        if (kycPolicy.isLocalDevelopmentEnabled()) {
            kyc.syncExternalReview(
                    applicant.reviewStatus(),
                    applicant.correlationId(),
                    KycReviewAnswer.GREEN,
                    "Local development KYC auto-approved",
                    null);
        }
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

    public KycVerificationSessionResult generateVerificationSession(String userId, String kycId) {
        if (!kycPolicy.usesManagedProvider()) {
            throw new IdentityException(IdentityErrorCode.KYC_MANAGED_EXTERNALLY,
                    "External KYC session is only available when a managed KYC provider is enabled");
        }

        IdentityUser user = requireUser(userId);
        KycProfile kyc = getKycByUser(kycId, userId);
        if (!kyc.isManagedExternally()) {
            throw new IdentityException(IdentityErrorCode.KYC_PROVIDER_NOT_CONFIGURED);
        }

        var session = externalKycVerificationPort.generateVerificationSession(
                user,
                kyc.getKycId(),
                kyc.getProviderApplicantId());
        return new KycVerificationSessionResult(
                kyc.getKycId(),
                session.provider(),
                session.applicantId(),
                session.levelName(),
                session.accessToken(),
                session.expiresInSeconds(),
                session.sandbox());
    }

    public void handleSumsubWebhook(
            byte[] payload,
            String digest,
            String digestAlgorithm,
            String providerApplicantId,
            String providerReviewStatus,
            String reviewAnswer,
            String moderationComment,
            String clientComment,
            String correlationId) {
        if (!kycPolicy.isSumsubEnabled()) {
            log.info("Ignoring Sumsub webhook because provider is not enabled");
            return;
        }

        externalKycVerificationPort.verifyWebhookSignature(payload, digest, digestAlgorithm);

        if (providerApplicantId == null || providerApplicantId.isBlank()) {
            throw new IdentityException(IdentityErrorCode.KYC_PROVIDER_ERROR, "Webhook is missing applicantId");
        }

        KycProfile kyc = kycPersistencePort.findByProviderApplicantId(providerApplicantId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.KYC_NOT_FOUND));
        kyc.syncExternalReview(
                providerReviewStatus,
                correlationId,
                KycReviewAnswer.from(reviewAnswer),
                moderationComment,
                clientComment);
        kycPersistencePort.save(kyc);
    }

    private KycProfile getKycByUser(String kycId, String userId) {
        return kycPersistencePort.findByKycIdAndUserId(kycId, userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.KYC_NOT_FOUND));
    }

    private void validateUserExists(String userId) {
        requireUser(userId);
    }

    private IdentityUser requireUser(String userId) {
        return userPersistencePort.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
    }

}
