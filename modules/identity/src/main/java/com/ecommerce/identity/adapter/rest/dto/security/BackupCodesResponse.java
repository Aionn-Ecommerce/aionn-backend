package com.ecommerce.identity.adapter.rest.dto.security;

import java.util.List;

public record BackupCodesResponse(
        List<String> backupCodes
) {
}
