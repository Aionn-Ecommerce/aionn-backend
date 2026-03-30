package com.ecommerce.identity.application.usecase.security;

import com.ecommerce.identity.application.dto.security.ChangePasswordCommand;
import com.ecommerce.identity.application.port.in.security.ChangePasswordInputPort;
import com.ecommerce.identity.application.service.SecurityService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChangePasswordUseCase implements ChangePasswordInputPort {

    private final SecurityService securityService;

    @Override
    public void execute(ChangePasswordCommand command) {
        securityService.changePassword(command.userId(), command.currentPassword(), command.newPassword(), command.clientIp());
    }
}
