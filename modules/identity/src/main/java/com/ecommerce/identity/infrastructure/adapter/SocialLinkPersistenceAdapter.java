package com.ecommerce.identity.infrastructure.adapter;

import com.ecommerce.identity.application.port.out.social.SocialLinkPersistencePort;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.model.SocialLink;
import com.ecommerce.identity.domain.valueobject.AuthProvider;
import com.ecommerce.identity.infrastructure.persistence.mapper.SocialLinkDomainMapper;
import com.ecommerce.identity.infrastructure.persistence.repository.auth.SocialAccountRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SocialLinkPersistenceAdapter implements SocialLinkPersistencePort {

    private final SocialAccountRepository socialAccountRepository;
    private final UserRepository userRepository;
    private final SocialLinkDomainMapper socialLinkDomainMapper;

    @Override
    public Optional<SocialLink> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId) {
        return socialAccountRepository.findByProviderAndProviderUserId(provider.name(), providerUserId)
                .map(socialLinkDomainMapper::toDomain);
    }

    @Override
    public Optional<SocialLink> findByUserIdAndProvider(String userId, AuthProvider provider) {
        return socialAccountRepository.findByUser_UserIdAndProvider(userId, provider.name())
                .map(socialLinkDomainMapper::toDomain);
    }

    @Override
    public boolean existsByProviderAndProviderUserId(AuthProvider provider, String providerUserId) {
        return socialAccountRepository.existsByProviderAndProviderUserId(provider.name(), providerUserId);
    }

    @Override
    public SocialLink save(SocialLink socialLink, String userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
        var entity = socialLinkDomainMapper.toEntity(socialLink, user);
        var savedEntity = socialAccountRepository.save(entity);
        return socialLinkDomainMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteByUserIdAndProvider(String userId, AuthProvider provider) {
        socialAccountRepository.deleteByUser_UserIdAndProvider(userId, provider.name());
    }
}
