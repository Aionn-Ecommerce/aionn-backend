package com.ecommerce.identity.application.usecase.auth;

import com.ecommerce.identity.application.dto.auth.result.RefreshAccessTokenResult;
import com.ecommerce.identity.application.dto.auth.command.RefreshTokenCommand;
import com.ecommerce.identity.application.port.in.auth.RefreshTokenInputPort;
import com.ecommerce.identity.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase implements RefreshTokenInputPort {

    private final AuthService authService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefreshAccessTokenResult execute(RefreshTokenCommand command) {
        authService.refreshToken(command);
        throw new UnsupportedOperationException("Refresh token not implemented yet");
    }
}
