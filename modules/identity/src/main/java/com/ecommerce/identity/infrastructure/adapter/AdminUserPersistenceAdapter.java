package com.ecommerce.identity.infrastructure.adapter;

import com.ecommerce.identity.application.port.out.admin.AdminUserPersistencePort;
import com.ecommerce.identity.domain.model.IdentityUser;
import com.ecommerce.identity.domain.valueobject.UserRole;
import com.ecommerce.identity.domain.valueobject.UserStatus;
import com.ecommerce.identity.infrastructure.persistence.mapper.IdentityUserMapper;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter implementation for admin user persistence operations.
 * Delegates to UserRepository and maps between domain and infrastructure
 * layers.
 */
@Component
@RequiredArgsConstructor
public class AdminUserPersistenceAdapter implements AdminUserPersistencePort {

    private final UserRepository userRepository;
    private final IdentityUserMapper identityUserMapper;

    @Override
    public Optional<IdentityUser> findById(String userId) {
        return userRepository.findById(userId)
                .map(identityUserMapper::toDomain);
    }

    @Override
    public IdentityUser save(IdentityUser user) {
        var entity = identityUserMapper.toEntity(user);
        var saved = userRepository.save(entity);
        return identityUserMapper.toDomain(saved);
    }

    @Override
    public Page<IdentityUser> findUsersWithFilters(UserStatus status, UserRole role, Pageable pageable) {
        return userRepository.findUsersWithFilters(status, role, pageable)
                .map(identityUserMapper::toDomain);
    }
}
