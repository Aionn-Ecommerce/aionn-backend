package com.ecommerce.identity.domain.model;

import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.valueobject.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class KycProfile {
    private final String kycId;
    private final String userId;
    private final String docType;
    private String blobUrl;
    private KycStatus status;
    private String adminId;
    private String reason;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private final LocalDateTime createdAt;

    public void uploadDocument(String blobUrl) {
        this.blobUrl = blobUrl;
    }

    public void submit() {
        validateTransition(KycStatus.SUBMITTED);
        this.status = KycStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    public void review(String adminId, String note) {
        validateTransition(KycStatus.IN_REVIEW);
        this.status = KycStatus.IN_REVIEW;
        this.adminId = adminId;
        this.reason = note;
    }

    public void approve(String adminId) {
        validateTransition(KycStatus.APPROVED);
        this.status = KycStatus.APPROVED;
        this.adminId = adminId;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject(String adminId, String reason) {
        validateTransition(KycStatus.REJECTED);
        this.status = KycStatus.REJECTED;
        this.adminId = adminId;
        this.reason = reason;
    }

    public boolean canBeCancelled() {
        return status.canBeCancelled();
    }

    private void validateTransition(KycStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IdentityException(
                    IdentityErrorCode.KYC_INVALID_STATUS_TRANSITION,
                    String.format("Cannot transition from %s to %s", status, newStatus));
        }
    }
}
