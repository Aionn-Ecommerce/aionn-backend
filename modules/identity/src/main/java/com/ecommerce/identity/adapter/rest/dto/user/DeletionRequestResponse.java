package com.ecommerce.identity.adapter.rest.dto.user;

import java.time.LocalDateTime;

public record DeletionRequestResponse(
        String requestId,
        String status,
        LocalDateTime requestedAt,
        LocalDateTime scheduledDeletionAt) {
}
