package com.aionn.identity.domain.model;

import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.identity.domain.valueobject.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * KYC profile aggregate. Field semantics:
 * <ul>
 * <li>{@code reviewerId} / {@code reviewNote}: set when an admin moves the
 * profile to {@link KycStatus#IN_REVIEW}.</li>
 * <li>{@code decisionAdminId}: admin who issued the final approve/reject.</li>
 * <li>{@code rejectReason}: only populated on {@link KycStatus#REJECTED}.</li>
 * <li>{@code approvedAt}: only populated on {@link KycStatus#APPROVED}.</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public class KycProfile {
    private final String kycId;
    private final String userId;
    private final String docType;
    private String blobUrl;
    private KycStatus status;
    private String reviewerId;
    private String reviewNote;
    private String decisionAdminId;
    private String rejectReason;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private final LocalDateTime createdAt;

    public void uploadDocument(String blobUrl) {
        if (blobUrl == null || blobUrl.isBlank()) {
            throw new IdentityException(IdentityErrorCode.AVATAR_URL_INVALID, "blobUrl must not be blank");
        }
        this.blobUrl = blobUrl;
    }

    public void submit() {
        if (blobUrl == null || blobUrl.isBlank() || "PENDING_UPLOAD".equals(blobUrl)) {
            throw new IdentityException(IdentityErrorCode.KYC_INVALID_STATUS_TRANSITION,
                    "Cannot submit without an uploaded document");
        }
        validateTransition(KycStatus.SUBMITTED);
        this.status = KycStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    public void review(String reviewerId, String note) {
        validateTransition(KycStatus.IN_REVIEW);
        this.status = KycStatus.IN_REVIEW;
        this.reviewerId = reviewerId;
        this.reviewNote = note;
    }

    public void approve(String adminId) {
        validateTransition(KycStatus.APPROVED);
        this.status = KycStatus.APPROVED;
        this.decisionAdminId = adminId;
        this.approvedAt = LocalDateTime.now();
        // Reject reason is no longer relevant on the approved record.
        this.rejectReason = null;
    }

    public void reject(String adminId, String reason) {
        validateTransition(KycStatus.REJECTED);
        this.status = KycStatus.REJECTED;
        this.decisionAdminId = adminId;
        this.rejectReason = reason;
        // Defensive: clear stale approval data if any.
        this.approvedAt = null;
    }

    public void cancel() {
        if (!canBeCancelled()) {
            throw new IdentityException(IdentityErrorCode.KYC_CANNOT_BE_CANCELLED);
        }
        this.status = KycStatus.CANCELLED;
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

