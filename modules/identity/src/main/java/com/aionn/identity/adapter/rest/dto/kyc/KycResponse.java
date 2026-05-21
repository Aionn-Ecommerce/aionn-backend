package com.aionn.identity.adapter.rest.dto.kyc;

import java.time.LocalDateTime;

public record KycResponse(
                String kycId,
                String userId,
                String docType,
                String blobUrl,
                String status,
                String reviewerId,
                String reviewNote,
                String decisionAdminId,
                String rejectReason,
                LocalDateTime submittedAt,
                LocalDateTime approvedAt) {
}

