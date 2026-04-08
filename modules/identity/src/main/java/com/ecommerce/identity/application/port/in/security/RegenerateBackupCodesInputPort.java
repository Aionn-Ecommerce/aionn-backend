package com.ecommerce.identity.application.port.in.security;

import com.ecommerce.identity.application.dto.security.result.BackupCodesResult;
import com.ecommerce.identity.application.dto.security.command.RegenerateBackupCodesCommand;

public interface RegenerateBackupCodesInputPort {
    BackupCodesResult execute(RegenerateBackupCodesCommand command);
}


