package com.ecommerce.identity.application.service;

import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.infrastructure.persistence.entity.KycProfileEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import com.ecommerce.identity.infrastructure.persistence.repository.kyc.KycProfileRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
import com.ecommerce.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KycService {

    private final KycProfileRepository kycRepository;
    private final UserRepository userRepository;

    @Transactional
    public KycProfileEntity createKyc(String userId, String docType) {
        UserEntity user = getUser(userId);
        KycProfileEntity entity = KycProfileEntity.builder()
                .kycId(IdGenerator.ulid())
                .user(user)
                .docType(docType)
                .blobUrl("PENDING_UPLOAD")
                .status("DRAFT")
                .build();
        return kycRepository.save(entity);
    }

    @Transactional
    public KycProfileEntity uploadDocument(String userId, String kycId, String blobUrl) {
        KycProfileEntity entity = getKycByUser(kycId, userId);
        entity.setBlobUrl(blobUrl);
        return kycRepository.save(entity);
    }

    @Transactional
    public KycProfileEntity submit(String userId, String kycId) {
        KycProfileEntity entity = getKycByUser(kycId, userId);
        entity.setStatus("SUBMITTED");
        entity.setSubmittedAt(LocalDateTime.now());
        return kycRepository.save(entity);
    }

    @Transactional
    public void cancel(String userId, String kycId) {
        KycProfileEntity entity = getKycByUser(kycId, userId);
        if (!"SUBMITTED".equals(entity.getStatus()) && !"DRAFT".equals(entity.getStatus())) {
            throw new IdentityException(IdentityErrorCode.VERIFICATION_TOKEN_INVALID, "KYC cannot be cancelled");
        }
        kycRepository.delete(entity);
    }

    @Transactional
    public KycProfileEntity review(String adminUserId, String kycId, String note) {
        getUser(adminUserId);
        KycProfileEntity entity = getKyc(kycId);
        entity.setStatus("IN_REVIEW");
        entity.setAdminId(adminUserId);
        entity.setReason(note);
        return kycRepository.save(entity);
    }

    @Transactional
    public KycProfileEntity approve(String adminUserId, String kycId) {
        getUser(adminUserId);
        KycProfileEntity entity = getKyc(kycId);
        entity.setStatus("APPROVED");
        entity.setAdminId(adminUserId);
        entity.setApprovedAt(LocalDateTime.now());
        return kycRepository.save(entity);
    }

    @Transactional
    public KycProfileEntity reject(String adminUserId, String kycId, String reason) {
        getUser(adminUserId);
        KycProfileEntity entity = getKyc(kycId);
        entity.setStatus("REJECTED");
        entity.setAdminId(adminUserId);
        entity.setReason(reason);
        return kycRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<KycProfileEntity> listMy(String userId) {
        getUser(userId);
        return kycRepository.findByUser_UserIdOrderBySubmittedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public KycProfileEntity get(String userId, String kycId) {
        getUser(userId);
        return getKycByUser(kycId, userId);
    }

    private KycProfileEntity getKycByUser(String kycId, String userId) {
        return kycRepository.findByKycIdAndUser_UserId(kycId, userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND, "KYC not found"));
    }

    private KycProfileEntity getKyc(String kycId) {
        return kycRepository.findById(kycId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND, "KYC not found"));
    }

    private UserEntity getUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
    }
}
