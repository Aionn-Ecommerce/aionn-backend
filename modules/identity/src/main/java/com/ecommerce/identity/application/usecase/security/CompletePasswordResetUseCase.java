package com.ecommerce.identity.application.usecase.security;

import com.ecommerce.identity.application.dto.security.CompletePasswordResetCommand;
import com.ecommerce.identity.application.port.in.security.CompletePasswordResetInputPort;
import com.ecommerce.identity.application.service.SecurityService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CompletePasswordResetUseCase implements CompletePasswordResetInputPort {

    private final SecurityService securityService;

    @Override
    public void execute(CompletePasswordResetCommand command) {
        securityService.completePasswordReset(command.token(), command.newPassword(), command.clientIp());
    }
}
