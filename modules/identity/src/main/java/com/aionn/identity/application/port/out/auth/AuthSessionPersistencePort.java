package com.aionn.identity.application.port.out.auth;

import com.aionn.identity.domain.model.AuthSession;

import java.util.List;
import java.util.Optional;

public interface AuthSessionPersistencePort {

    AuthSession save(AuthSession session);

    Optional<AuthSession> findById(String sessionId);

    List<AuthSession> findByUserId(String userId);

    List<AuthSession> saveAll(List<AuthSession> sessions);
}

