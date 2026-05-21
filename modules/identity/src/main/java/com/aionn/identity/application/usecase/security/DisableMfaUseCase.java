package com.aionn.identity.application.usecase.security;

import com.aionn.identity.application.dto.security.result.MfaResult;
import com.aionn.identity.application.dto.security.command.DisableMfaCommand;
import com.aionn.identity.application.port.in.security.DisableMfaInputPort;
import com.aionn.identity.application.service.MfaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DisableMfaUseCase implements DisableMfaInputPort {

    private final MfaService mfaService;

    @Override
    public MfaResult execute(DisableMfaCommand command) {
        var result = mfaService.disableMfa(command.userId(), command.password(), command.clientIp());
        return new MfaResult(result);
    }
}

