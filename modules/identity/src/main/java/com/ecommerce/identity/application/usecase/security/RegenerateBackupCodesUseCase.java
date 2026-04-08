package com.ecommerce.identity.application.usecase.security;

import com.ecommerce.identity.application.dto.security.result.BackupCodesResult;
import com.ecommerce.identity.application.dto.security.command.RegenerateBackupCodesCommand;
import com.ecommerce.identity.application.port.in.security.RegenerateBackupCodesInputPort;
import com.ecommerce.identity.application.service.MfaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegenerateBackupCodesUseCase implements RegenerateBackupCodesInputPort {

    private final MfaService mfaService;

    @Override
    public BackupCodesResult execute(RegenerateBackupCodesCommand command) {
        var result = mfaService.regenerateBackupCodes(command.userId(), command.clientIp());
        return new BackupCodesResult(result);
    }
}
