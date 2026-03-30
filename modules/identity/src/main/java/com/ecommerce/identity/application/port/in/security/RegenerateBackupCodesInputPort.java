package com.ecommerce.identity.application.port.in.security;

import com.ecommerce.identity.application.dto.security.RegenerateBackupCodesCommand;
import com.ecommerce.identity.application.dto.security.BackupCodesResult;

public interface RegenerateBackupCodesInputPort {
    BackupCodesResult execute(RegenerateBackupCodesCommand command);
}