package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.dto.auth.LinkSocialCommand;
import com.ecommerce.identity.application.dto.auth.LoginCommand;
import com.ecommerce.identity.application.dto.auth.LogoutAllCommand;
import com.ecommerce.identity.application.dto.auth.LogoutCommand;
import com.ecommerce.identity.application.dto.auth.RefreshTokenCommand;
import com.ecommerce.identity.application.dto.auth.RevokeSessionCommand;
import com.ecommerce.identity.application.dto.auth.SocialLoginCommand;
import com.ecommerce.identity.application.port.out.auth.AccessTokenIssuer;
import com.ecommerce.identity.application.port.out.auth.SocialTokenVerifier;
import com.ecommerce.identity.application.port.out.security.PasswordHasher;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.id.UserId;
import com.ecommerce.identity.domain.model.AuthSession;
import com.ecommerce.identity.domain.model.IdentityUser;
import com.ecommerce.identity.domain.model.SocialLink;
import com.ecommerce.identity.domain.valueobject.AuthProvider;
import com.ecommerce.identity.domain.valueobject.AuthSessionStatus;
import com.ecommerce.identity.infrastructure.persistence.entity.AuthSessionEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.SocialAccountEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import com.ecommerce.identity.infrastructure.persistence.mapper.AuthSessionDomainMapper;
import com.ecommerce.identity.infrastructure.persistence.mapper.IdentityUserMapper;
import com.ecommerce.identity.infrastructure.persistence.mapper.SocialLinkDomainMapper;
import com.ecommerce.identity.infrastructure.persistence.repository.auth.AuthSessionRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.auth.SocialAccountRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
import com.ecommerce.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long SESSION_EXPIRES_DAYS = 30;

    private final UserRepository userRepository;
    private final AuthSessionRepository authSessionRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final PasswordHasher passwordHasher;
    private final AccessTokenIssuer accessTokenIssuer;
    private final SocialTokenVerifier socialTokenVerifier;
    private final IdentityUserMapper identityUserMapper;
    private final AuthSessionDomainMapper authSessionDomainMapper;
    private final SocialLinkDomainMapper socialLinkDomainMapper;

    @Transactional
    public AuthSessionEntity login(LoginCommand command) {
        UserEntity user = findByIdentity(command.identity())
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.INVALID_CREDENTIALS));

        validateActiveUser(user);
        if (user.getPasswordHash() == null || !passwordHasher.matches(command.password(), user.getPasswordHash())) {
            throw new IdentityException(IdentityErrorCode.INVALID_CREDENTIALS);
        }

        return createSession(user, command.ipAddress(), command.userAgent());
    }

    @Transactional
    public AuthSessionEntity socialLogin(SocialLoginCommand command) {
        AuthProvider provider = AuthProvider.from(command.provider());
        String providerUserId = socialTokenVerifier.verifyAndExtractProviderUserId(provider, command.providerToken());

        UserEntity user;
        var socialLink = socialAccountRepository.findByProviderAndProviderUserId(provider.name(), providerUserId);
        if (socialLink.isPresent()) {
            user = socialLink.get().getUser();
        } else {
            user = createUserForSocial(provider, providerUserId);
            SocialLink domainSocialLink = SocialLink.createNew(
                    IdGenerator.ulid(),
                    user.getUserId(),
                    provider,
                    providerUserId);
            socialAccountRepository.save(socialLinkDomainMapper.toEntity(domainSocialLink, user));
        }

        validateActiveUser(user);
        return createSession(user, command.ipAddress(), command.userAgent());
    }

    @Transactional(readOnly = true)
    public List<AuthSessionEntity> listSessions(String userId) {
        validateUserExists(userId);
        return authSessionRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public SocialAccountEntity linkSocial(LinkSocialCommand command) {
        UserEntity user = validateUserExists(command.userId());
        AuthProvider provider = AuthProvider.from(command.provider());
        String providerUserId = socialTokenVerifier.verifyAndExtractProviderUserId(provider, command.providerToken());

        if (socialAccountRepository.existsByProviderAndProviderUserId(provider.name(), providerUserId)) {
            throw new IdentityException(IdentityErrorCode.SOCIAL_LINK_EXISTS);
        }
        if (socialAccountRepository.findByUser_UserIdAndProvider(command.userId(), provider.name()).isPresent()) {
            throw new IdentityException(IdentityErrorCode.SOCIAL_LINK_EXISTS);
        }

        SocialLink domainSocialLink = SocialLink.createNew(
                IdGenerator.ulid(),
                user.getUserId(),
                provider,
                providerUserId);
        return socialAccountRepository.save(socialLinkDomainMapper.toEntity(domainSocialLink, user));
    }

    @Transactional
    public void unlinkSocial(String userId, String providerRaw) {
        validateUserExists(userId);
        AuthProvider provider = AuthProvider.from(providerRaw);
        socialAccountRepository.findByUser_UserIdAndProvider(userId, provider.name())
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.SOCIAL_LINK_NOT_FOUND));
        socialAccountRepository.deleteByUser_UserIdAndProvider(userId, provider.name());
    }

    @Transactional
    public void revokeSession(RevokeSessionCommand command) {
        revokeSessionInternal(command.userId(), command.sessionId());
    }

    @Transactional
    public void logout(LogoutCommand command) {
        revokeSessionInternal(command.userId(), command.sessionId());
    }

    @Transactional
    public int logoutAll(LogoutAllCommand command) {
        validateUserExists(command.userId());
        int revoked = 0;
        var sessions = authSessionRepository.findByUser_UserIdOrderByCreatedAtDesc(command.userId());
        for (AuthSessionEntity session : sessions) {
            AuthSession domainSession = authSessionDomainMapper.toDomain(session);
            if (AuthSessionStatus.ACTIVE.equals(domainSession.getStatus())) {
                domainSession.revoke();
                AuthSessionEntity revokedSession = authSessionDomainMapper.toEntity(domainSession, session.getUser());
                session.setStatus(revokedSession.getStatus());
                revoked++;
            }
        }
        authSessionRepository.saveAll(sessions);
        return revoked;
    }

    @Transactional
    public AuthSessionEntity refreshToken(RefreshTokenCommand command) {
        // TODO: Implement refresh token logic
        throw new UnsupportedOperationException("Refresh token logic is not implemented yet.");
    }

    public String issueAccessToken(String userId, String sessionId, LocalDateTime expiresAt) {
        return accessTokenIssuer.issueAccessToken(userId, sessionId, expiresAt);
    }

    // Helper methods
    private void revokeSessionInternal(String userId, String sessionId) {
        validateUserExists(userId);
        AuthSessionEntity session = authSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.SESSION_NOT_FOUND));

        if (!session.getUser().getUserId().equals(userId)) {
            throw new IdentityException(IdentityErrorCode.SESSION_FORBIDDEN);
        }

        AuthSession domainSession = authSessionDomainMapper.toDomain(session);
        if (AuthSessionStatus.ACTIVE.equals(domainSession.getStatus())) {
            domainSession.revoke();
            AuthSessionEntity revokedSession = authSessionDomainMapper.toEntity(domainSession, session.getUser());
            session.setStatus(revokedSession.getStatus());
            authSessionRepository.save(session);
        }
    }

    private UserEntity validateUserExists(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
        validateActiveUser(user);
        return user;
    }

    private void validateActiveUser(UserEntity user) {
        IdentityUser domainUser = identityUserMapper.toDomain(user);
        if (!domainUser.isActive()) {
            throw new IdentityException(IdentityErrorCode.USER_INACTIVE);
        }
    }

    private UserEntity createUserForSocial(AuthProvider provider, String providerUserId) {
        IdentityUser domainUser = IdentityUser.createNew(
                UserId.of(IdGenerator.ulid()),
                null,
                null,
                generateSocialUsername(provider, providerUserId));
        return userRepository.save(identityUserMapper.toEntity(domainUser));
    }

    private String generateSocialUsername(AuthProvider provider, String providerUserId) {
        String base = (provider.name().toLowerCase() + "_" + providerUserId.replace(':', '_')
                .replaceAll("[^a-zA-Z0-9_]", "")).toLowerCase();
        if (base.length() > 40) {
            base = base.substring(0, 40);
        }
        String candidate = base;
        int suffix = 1;
        while (userRepository.findByUsernameIgnoreCase(candidate).isPresent()) {
            candidate = base + "_" + suffix++;
            if (candidate.length() > 50) {
                candidate = candidate.substring(0, 50);
            }
        }
        return candidate;
    }

    private AuthSessionEntity createSession(UserEntity user, String ipAddress, String userAgent) {
        AuthSession domainSession = AuthSession.createNew(
                IdGenerator.ulid(),
                user.getUserId(),
                ipAddress,
                userAgent,
                LocalDateTime.now().plusDays(SESSION_EXPIRES_DAYS));
        return authSessionRepository.save(authSessionDomainMapper.toEntity(domainSession, user));
    }

    private java.util.Optional<UserEntity> findByIdentity(String identity) {
        return userRepository.findByEmailIgnoreCase(identity)
                .or(() -> userRepository.findByPhone(identity))
                .or(() -> userRepository.findByUsernameIgnoreCase(identity));
    }
}
