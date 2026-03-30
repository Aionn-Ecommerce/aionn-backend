package com.ecommerce.identity.application.usecase.security;

import com.ecommerce.identity.application.dto.security.PasswordResetResult;
import com.ecommerce.identity.application.dto.security.RequestPasswordResetCommand;
import com.ecommerce.identity.application.port.in.security.RequestPasswordResetInputPort;
import com.ecommerce.identity.application.service.SecurityService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RequestPasswordResetUseCase implements RequestPasswordResetInputPort {

    private final SecurityService securityService;

    @Override
    public PasswordResetResult execute(RequestPasswordResetCommand command) {
        var result = securityService.requestPasswordReset(command.identity(), command.clientIp());
        return new PasswordResetResult(result);
    }
}
