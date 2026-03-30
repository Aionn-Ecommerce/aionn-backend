package com.ecommerce.identity.application.dto.user;

import java.time.LocalDateTime;

public record DataExportRequestView(
        String requestId,
        String status,
        LocalDateTime requestedAt) {
}
