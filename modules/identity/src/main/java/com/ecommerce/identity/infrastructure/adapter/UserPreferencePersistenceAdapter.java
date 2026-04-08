package com.ecommerce.identity.infrastructure.adapter;

import com.ecommerce.identity.application.dto.preference.result.UserPreferenceResult;
import com.ecommerce.identity.application.port.out.preference.UserPreferencePersistencePort;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.infrastructure.persistence.entity.UserPreferenceEntity;
import com.ecommerce.identity.infrastructure.persistence.mapper.UserPreferenceDomainMapper;
import com.ecommerce.identity.infrastructure.persistence.repository.preference.UserPreferenceRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPreferencePersistenceAdapter implements UserPreferencePersistencePort {

    private final UserPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final UserPreferenceDomainMapper mapper;

    @Override
    public UserPreferenceResult save(UserPreferenceResult preference) {
        UserPreferenceEntity entity = mapper.toEntity(preference);
        UserPreferenceEntity saved = preferenceRepository.save(entity);
        return mapper.toResult(saved);
    }

    @Override
    public Optional<UserPreferenceResult> findById(String userId) {
        return preferenceRepository.findById(userId)
                .map(mapper::toResult);
    }

    @Override
    public UserPreferenceResult createDefault(String userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));

        UserPreferenceEntity entity = UserPreferenceEntity.builder()
                .userId(userId)
                .user(user)
                .build();

        UserPreferenceEntity saved = preferenceRepository.save(entity);
        return mapper.toResult(saved);
    }
}
