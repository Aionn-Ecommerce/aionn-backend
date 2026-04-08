package com.ecommerce.identity.application.usecase.auth;

import com.ecommerce.identity.application.dto.auth.command.SocialLoginCommand;
import com.ecommerce.identity.application.dto.auth.result.SocialLoginResult;
import com.ecommerce.identity.application.port.in.auth.SocialAuthInputPort;
import com.ecommerce.identity.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SocialLoginUseCase implements SocialAuthInputPort {

    private final AuthService authService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SocialLoginResult execute(SocialLoginCommand command) {
        return authService.socialLogin(command);
    }
}
