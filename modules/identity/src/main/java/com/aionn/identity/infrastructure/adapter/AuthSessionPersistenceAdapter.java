package com.aionn.identity.infrastructure.adapter;

import com.aionn.identity.application.port.out.auth.AuthSessionPersistencePort;
import com.aionn.identity.domain.model.AuthSession;
import com.aionn.identity.infrastructure.persistence.entity.UserEntity;
import com.aionn.identity.infrastructure.persistence.mapper.AuthSessionDomainMapper;
import com.aionn.identity.infrastructure.persistence.repository.auth.AuthSessionRepository;
import com.aionn.identity.infrastructure.persistence.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthSessionPersistenceAdapter implements AuthSessionPersistencePort {

    private final AuthSessionRepository authSessionRepository;
    private final UserRepository userRepository;
    private final AuthSessionDomainMapper authSessionDomainMapper;

    @Override
    public AuthSession save(AuthSession session) {
        UserEntity userEntity = userRepository.getReferenceById(session.getUserId());
        var entity = authSessionDomainMapper.toEntity(session, userEntity);
        var savedEntity = authSessionRepository.save(entity);
        return authSessionDomainMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<AuthSession> findById(String sessionId) {
        return authSessionRepository.findById(sessionId)
                .map(authSessionDomainMapper::toDomain);
    }

    @Override
    public List<AuthSession> findByUserId(String userId) {
        return authSessionRepository.findByUser_UserIdOrderByCreatedAtDesc(userId).stream()
                .map(authSessionDomainMapper::toDomain)
                .toList();
    }

    @Override
    public List<AuthSession> saveAll(List<AuthSession> sessions) {
        var entities = sessions.stream()
                .map(session -> {
                    UserEntity userEntity = userRepository.getReferenceById(session.getUserId());
                    return authSessionDomainMapper.toEntity(session, userEntity);
                })
                .toList();
        var savedEntities = authSessionRepository.saveAll(entities);
        return savedEntities.stream()
                .map(authSessionDomainMapper::toDomain)
                .toList();
    }
}

