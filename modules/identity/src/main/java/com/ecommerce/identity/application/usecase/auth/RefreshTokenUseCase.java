package com.ecommerce.identity.application.usecase.auth;

import com.ecommerce.identity.adapter.rest.mapper.auth.AuthDtoMapper;
import com.ecommerce.identity.application.dto.auth.LoginResult;
import com.ecommerce.identity.application.dto.auth.RefreshTokenCommand;
import com.ecommerce.identity.application.port.in.auth.RefreshTokenInputPort;
import com.ecommerce.identity.application.service.AuthService;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase implements RefreshTokenInputPort {

    private final AuthService authService;
    private final AuthDtoMapper authDtoMapper;

    @Override
    @Transactional
    public LoginResult execute(RefreshTokenCommand command) {
        var session = authService.refreshToken(command);
        String accessToken = authService.issueAccessToken(
                session.getUser().getUserId(),
                session.getSessionId(),
                session.getExpiresAt());
        return authDtoMapper.toLoginResult(session, accessToken);
    }

    @Override
    @Transactional
    public com.ecommerce.identity.application.dto.auth.RefreshAccessTokenResult refreshToken(String userId,
            String sessionId) {
        throw new IdentityException(IdentityErrorCode.NOT_IMPLEMENTED);
    }
}
