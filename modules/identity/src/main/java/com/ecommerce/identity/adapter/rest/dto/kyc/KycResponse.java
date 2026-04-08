package com.ecommerce.identity.adapter.rest.dto.kyc;

import java.time.LocalDateTime;

public record KycResponse(
        String kycId,
        String userId,
        String docType,
        String blobUrl,
        String status,
        String adminId,
        String reason,
        LocalDateTime submittedAt,
        LocalDateTime approvedAt) {
}


