package com.ecommerce.identity.adapter.rest.dto.user;

import java.time.LocalDateTime;

public record DataExportRequestResponse(
        String requestId,
        String status,
        LocalDateTime requestedAt) {
}


