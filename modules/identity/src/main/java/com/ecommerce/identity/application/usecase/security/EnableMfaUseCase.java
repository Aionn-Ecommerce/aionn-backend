package com.ecommerce.identity.application.usecase.security;

import com.ecommerce.identity.application.dto.security.EnableMfaCommand;
import com.ecommerce.identity.application.dto.security.MfaResult;
import com.ecommerce.identity.application.port.in.security.EnableMfaInputPort;
import com.ecommerce.identity.application.service.SecurityService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EnableMfaUseCase implements EnableMfaInputPort {

    private final SecurityService securityService;

    @Override
    public MfaResult execute(EnableMfaCommand command) {
        var result = securityService.enableMfa(command.userId(), command.password(), command.clientIp());
        return new MfaResult(result);
    }
}
