package com.ecommerce.identity.application.usecase.security;

import com.ecommerce.identity.application.dto.security.command.ChangePasswordCommand;
import com.ecommerce.identity.application.port.in.security.ChangePasswordInputPort;
import com.ecommerce.identity.application.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase implements ChangePasswordInputPort {

    private final PasswordResetService passwordResetService;

    @Override
    public void execute(ChangePasswordCommand command) {
        passwordResetService.changePassword(command.userId(), command.currentPassword(), command.newPassword(),
                command.clientIp());
    }
}
