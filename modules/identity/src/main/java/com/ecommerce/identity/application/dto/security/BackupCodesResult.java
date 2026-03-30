package com.ecommerce.identity.application.dto.security;

import java.util.List;

public record BackupCodesResult(
        List<String> backupCodes
) {
}
