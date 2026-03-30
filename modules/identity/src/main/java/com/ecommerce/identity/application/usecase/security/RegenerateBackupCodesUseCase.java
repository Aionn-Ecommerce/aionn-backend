package com.ecommerce.identity.application.usecase.security;

import com.ecommerce.identity.application.dto.security.BackupCodesResult;
import com.ecommerce.identity.application.dto.security.RegenerateBackupCodesCommand;
import com.ecommerce.identity.application.port.in.security.RegenerateBackupCodesInputPort;
import com.ecommerce.identity.application.service.SecurityService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegenerateBackupCodesUseCase implements RegenerateBackupCodesInputPort {

    private final SecurityService securityService;

    @Override
    public BackupCodesResult execute(RegenerateBackupCodesCommand command) {
        var result = securityService.regenerateBackupCodes(command.userId(), command.clientIp());
        return new BackupCodesResult(result);
    }
}
