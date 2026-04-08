package com.ecommerce.identity.application.usecase.security;

import com.ecommerce.identity.application.dto.security.result.MfaResult;
import com.ecommerce.identity.application.dto.security.command.DisableMfaCommand;
import com.ecommerce.identity.application.port.in.security.DisableMfaInputPort;
import com.ecommerce.identity.application.service.MfaService;
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
