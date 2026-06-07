package com.aionn.identity.infrastructure.persistence.adapter.user;

import com.aionn.identity.domain.model.IdentityUser;
import com.aionn.identity.infrastructure.persistence.entity.UserEntity;
import com.aionn.identity.infrastructure.persistence.mapper.IdentityUserMapper;
import com.aionn.identity.infrastructure.persistence.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class IdentityUserEntitySaveSupport {

    private final UserRepository userRepository;
    private final IdentityUserMapper identityUserMapper;

    IdentityUser save(IdentityUser user) {
        UserEntity entity = identityUserMapper.toEntity(user);
        userRepository.findById(user.getUserId()).ifPresent(existing -> {
            entity.setMfaEnabled(existing.isMfaEnabled());
            entity.setMfaSecret(existing.getMfaSecret());
            entity.setFailedLoginAttempts(existing.getFailedLoginAttempts());
        });
        UserEntity savedEntity = userRepository.save(entity);
        return identityUserMapper.toDomain(savedEntity);
    }
}
