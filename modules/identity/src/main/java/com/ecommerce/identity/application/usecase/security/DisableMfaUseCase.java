package com.ecommerce.identity.application.usecase.security;

import com.ecommerce.identity.application.dto.security.DisableMfaCommand;
import com.ecommerce.identity.application.dto.security.MfaResult;
import com.ecommerce.identity.application.port.in.security.DisableMfaInputPort;
import com.ecommerce.identity.application.service.SecurityService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DisableMfaUseCase implements DisableMfaInputPort {

    private final SecurityService securityService;

    @Override
    public MfaResult execute(DisableMfaCommand command) {
        var result = securityService.disableMfa(command.userId(), command.password(), command.clientIp());
        return new MfaResult(result);
    }
}
