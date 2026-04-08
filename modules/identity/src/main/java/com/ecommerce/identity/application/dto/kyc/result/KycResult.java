package com.ecommerce.identity.application.dto.kyc.result;

import java.time.LocalDateTime;

public record KycResult(
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
