package com.aionn.identity.domain.model;

import com.aionn.identity.domain.valueobject.KycReviewAnswer;
import com.aionn.identity.domain.valueobject.KycStatus;
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
    private String provider;
    private String providerApplicantId;
    private String providerLevelName;
    private String providerReviewStatus;
    private String providerCorrelationId;
    private String reviewerId;
    private String reviewNote;
    private String decisionAdminId;
    private String rejectReason;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private final LocalDateTime createdAt;

    public void attachExternalProvider(
            String provider,
            String providerApplicantId,
            String providerLevelName,
            String providerReviewStatus,
            String providerCorrelationId) {
        this.provider = provider;
        this.providerApplicantId = providerApplicantId;
        this.providerLevelName = providerLevelName;
        this.providerReviewStatus = providerReviewStatus;
        this.providerCorrelationId = providerCorrelationId;
        this.status = KycStatus.SUBMITTED;
        if (this.submittedAt == null) {
            this.submittedAt = LocalDateTime.now();
        }
    }

    public void syncExternalReview(
            String providerReviewStatus,
            String providerCorrelationId,
            KycReviewAnswer reviewAnswer,
            String moderationComment,
            String clientComment) {
        this.providerReviewStatus = providerReviewStatus;
        this.providerCorrelationId = providerCorrelationId;
        this.reviewNote = moderationComment;

        if (reviewAnswer == null) {
            if (this.status == KycStatus.DRAFT) {
                this.status = KycStatus.SUBMITTED;
                this.submittedAt = this.submittedAt == null ? LocalDateTime.now() : this.submittedAt;
            }
            return;
        }

        switch (reviewAnswer) {
            case GREEN -> {
                this.status = KycStatus.APPROVED;
                this.decisionAdminId = providerDecisionSource();
                this.rejectReason = null;
                this.approvedAt = LocalDateTime.now();
            }
            case RED -> {
                this.status = KycStatus.REJECTED;
                this.decisionAdminId = providerDecisionSource();
                this.rejectReason = moderationComment != null && !moderationComment.isBlank()
                        ? moderationComment
                        : clientComment;
                this.approvedAt = null;
            }
        }
    }

    public boolean isManagedExternally() {
        return provider != null && !provider.isBlank();
    }

    private String providerDecisionSource() {
        return provider == null || provider.isBlank()
                ? "SYSTEM"
                : provider.toUpperCase();
    }
}
