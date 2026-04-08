package com.ecommerce.identity.infrastructure.adapter;

import com.ecommerce.identity.application.port.out.user.UserPersistencePort;
import com.ecommerce.identity.domain.model.IdentityUser;
import com.ecommerce.identity.infrastructure.persistence.mapper.IdentityUserMapper;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {

    private final UserRepository userRepository;
    private final IdentityUserMapper identityUserMapper;

    @Override
    public IdentityUser save(IdentityUser user) {
        var entity = identityUserMapper.toEntity(user);
        var savedEntity = userRepository.save(entity);
        return identityUserMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<IdentityUser> findById(String userId) {
        return userRepository.findById(userId)
                .map(identityUserMapper::toDomain);
    }

    @Override
    public Optional<IdentityUser> findByIdentity(String identity) {
        return userRepository.findByEmailIgnoreCase(identity)
                .or(() -> userRepository.findByPhone(identity))
                .or(() -> userRepository.findByUsernameIgnoreCase(identity))
                .map(identityUserMapper::toDomain);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsernameIgnoreCase(username);
    }

    @Override
    public boolean existsById(String userId) {
        return userRepository.existsById(userId);
    }
}
