package com.ecommerce.identity.infrastructure.persistence.repository;

import com.ecommerce.identity.domain.model.IdentityUser;
import com.ecommerce.identity.domain.repository.IdentityUserRepository;
import com.ecommerce.identity.infrastructure.persistence.mapper.IdentityUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaIdentityUserRepository implements IdentityUserRepository {

    private final SpringDataIdentityUserJpaRepository jpaRepository;
    private final IdentityUserMapper identityUserMapper;

    @Override
    public Optional<IdentityUser> findByEmail(String email) {
        return jpaRepository.findByEmailIgnoreCase(email)
                .map(identityUserMapper::toDomain);
    }

    @Override
    public Optional<IdentityUser> findByPhone(String phone) {
        return jpaRepository.findByPhone(phone)
                .map(identityUserMapper::toDomain);
    }

    @Override
    public IdentityUser save(IdentityUser user) {
        var saved = jpaRepository.save(identityUserMapper.toEntity(user));
        return identityUserMapper.toDomain(saved);
    }
}
