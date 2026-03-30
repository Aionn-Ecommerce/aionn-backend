package com.ecommerce.identity.application.dto.user;

import java.time.LocalDateTime;

public record DeletionRequestView(
        String requestId,
        String status,
        LocalDateTime requestedAt,
        LocalDateTime scheduledDeletionAt) {
}
