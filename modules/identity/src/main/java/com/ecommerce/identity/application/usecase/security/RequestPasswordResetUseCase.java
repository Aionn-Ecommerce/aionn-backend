package com.ecommerce.identity.application.usecase.security;

import com.ecommerce.identity.application.dto.security.result.PasswordResetResult;
import com.ecommerce.identity.application.dto.security.command.RequestPasswordResetCommand;
import com.ecommerce.identity.application.port.in.security.RequestPasswordResetInputPort;
import com.ecommerce.identity.application.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestPasswordResetUseCase implements RequestPasswordResetInputPort {

    private final PasswordResetService passwordResetService;

    @Override
    public PasswordResetResult execute(RequestPasswordResetCommand command) {
        var result = passwordResetService.requestPasswordReset(command.identity(), command.clientIp());
        return new PasswordResetResult(result);
    }
}
