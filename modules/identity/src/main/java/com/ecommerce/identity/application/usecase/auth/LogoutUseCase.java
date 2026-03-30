package com.ecommerce.identity.application.usecase.auth;

import com.ecommerce.identity.application.dto.auth.LogoutCommand;
import com.ecommerce.identity.application.port.in.auth.LogoutInputPort;
import com.ecommerce.identity.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutUseCase implements LogoutInputPort {

    private final AuthService authService;

    @Override
    @Transactional
    public void execute(LogoutCommand command) {
        authService.logout(command);
    }
}
