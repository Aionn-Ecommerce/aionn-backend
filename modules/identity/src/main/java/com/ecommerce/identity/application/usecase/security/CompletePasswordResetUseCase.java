package com.ecommerce.identity.application.usecase.security;

import com.ecommerce.identity.application.dto.security.command.CompletePasswordResetCommand;
import com.ecommerce.identity.application.port.in.security.CompletePasswordResetInputPort;
import com.ecommerce.identity.application.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompletePasswordResetUseCase implements CompletePasswordResetInputPort {

    private final PasswordResetService passwordResetService;

    @Override
    public void execute(CompletePasswordResetCommand command) {
        passwordResetService.completePasswordReset(command.token(), command.newPassword(), command.clientIp());
    }
}
