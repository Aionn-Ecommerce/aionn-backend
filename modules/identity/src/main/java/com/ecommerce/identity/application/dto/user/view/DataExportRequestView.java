package com.ecommerce.identity.application.dto.user.view;

import java.time.LocalDateTime;

public record DataExportRequestView(
                String requestId,
                String status,
                LocalDateTime requestedAt) {
}


