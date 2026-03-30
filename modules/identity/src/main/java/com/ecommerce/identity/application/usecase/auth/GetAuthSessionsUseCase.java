package com.ecommerce.identity.application.usecase.auth;

import com.ecommerce.identity.adapter.rest.mapper.auth.AuthDtoMapper;
import com.ecommerce.identity.application.dto.auth.AuthSessionView;
import com.ecommerce.identity.application.dto.auth.GetAuthSessionsQuery;
import com.ecommerce.identity.application.port.in.auth.GetAuthSessionsQueryPort;
import com.ecommerce.identity.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAuthSessionsUseCase implements GetAuthSessionsQueryPort {

    private final AuthService authService;
    private final AuthDtoMapper authDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AuthSessionView> execute(GetAuthSessionsQuery query) {
        var sessions = authService.listSessions(query.userId());
        return authDtoMapper.toAuthSessionViews(sessions);
    }
}
