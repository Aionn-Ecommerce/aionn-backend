package com.aionn.identity.application.usecase.auth;

import com.aionn.identity.adapter.rest.mapper.auth.AuthDtoMapper;
import com.aionn.identity.application.dto.auth.query.GetAuthSessionsQuery;
import com.aionn.identity.application.dto.auth.result.AuthSessionResult;
import com.aionn.identity.application.port.in.auth.GetAuthSessionsQueryPort;
import com.aionn.identity.application.service.AuthService;
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
    public List<AuthSessionResult> execute(GetAuthSessionsQuery query) {
        return authDtoMapper.toAuthSessionResults(authService.listSessions(query.userId()));
    }
}

