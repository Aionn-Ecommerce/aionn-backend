package com.ecommerce.identity.application.usecase.auth;

import com.ecommerce.identity.adapter.rest.mapper.auth.AuthDtoMapper;
import com.ecommerce.identity.application.dto.auth.LogoutAllCommand;
import com.ecommerce.identity.application.dto.auth.LogoutAllResult;
import com.ecommerce.identity.application.port.in.auth.LogoutAllInputPort;
import com.ecommerce.identity.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutAllUseCase implements LogoutAllInputPort {

    private final AuthService authService;
    private final AuthDtoMapper authDtoMapper;

    @Override
    @Transactional
    public LogoutAllResult execute(LogoutAllCommand command) {
        int revokedCount = authService.logoutAll(command);
        return authDtoMapper.toLogoutAllResult(revokedCount);
    }
}
