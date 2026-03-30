package com.ecommerce.identity.application.usecase.auth;

import com.ecommerce.identity.adapter.rest.mapper.auth.AuthDtoMapper;
import com.ecommerce.identity.application.dto.auth.LoginCommand;
import com.ecommerce.identity.application.dto.auth.LoginResult;
import com.ecommerce.identity.application.port.in.auth.LoginInputPort;
import com.ecommerce.identity.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginUseCase implements LoginInputPort {

    private final AuthService authService;
    private final AuthDtoMapper authDtoMapper;

    @Override
    @Transactional
    public LoginResult execute(LoginCommand command) {
        var session = authService.login(command);
        String accessToken = authService.issueAccessToken(
                session.getUser().getUserId(),
                session.getSessionId(),
                session.getExpiresAt());
        return authDtoMapper.toLoginResult(session, accessToken);
    }
}
