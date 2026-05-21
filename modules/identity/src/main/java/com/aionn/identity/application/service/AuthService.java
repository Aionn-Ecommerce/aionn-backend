package com.aionn.identity.application.service;

import com.aionn.identity.application.dto.auth.command.LinkSocialCommand;
import com.aionn.identity.application.dto.auth.command.LoginCommand;
import com.aionn.identity.application.dto.auth.command.LogoutAllCommand;
import com.aionn.identity.application.dto.auth.command.LogoutCommand;
import com.aionn.identity.application.dto.auth.command.RefreshTokenCommand;
import com.aionn.identity.application.dto.auth.command.RevokeSessionCommand;
import com.aionn.identity.application.dto.auth.command.SocialLoginCommand;
import com.aionn.identity.application.dto.auth.command.UnlinkSocialCommand;
import com.aionn.identity.application.dto.auth.result.LoginResult;
import com.aionn.identity.application.dto.auth.result.LogoutAllResult;
import com.aionn.identity.application.dto.auth.result.RefreshAccessTokenResult;
import com.aionn.identity.application.dto.auth.result.SocialLoginResult;
import com.aionn.identity.application.mapper.AuthResultMapper;
import com.aionn.identity.application.port.out.auth.AccessTokenIssuer;
import com.aionn.identity.application.port.out.auth.AuthPolicy;
import com.aionn.identity.application.port.out.auth.AuthSessionPersistencePort;
import com.aionn.identity.application.port.out.auth.RefreshTokenStore;
import com.aionn.identity.application.port.out.auth.SocialTokenVerifier;
import com.aionn.identity.application.port.out.auth.TokenBlacklist;
import com.aionn.identity.application.port.out.security.PasswordHasher;
import com.aionn.identity.application.port.out.social.SocialLinkPersistencePort;
import com.aionn.identity.application.port.out.user.UserPersistencePort;
import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.identity.domain.id.UserId;
import com.aionn.identity.domain.model.AuthSession;
import com.aionn.identity.domain.model.IdentityUser;
import com.aionn.identity.domain.model.SocialLink;
import com.aionn.identity.domain.valueobject.AuthProvider;
import com.aionn.identity.domain.valueobject.AuthSessionStatus;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

/**
 * Authentication service: login, social login, session lifecycle, refresh
 * tokens. Mapping to result DTOs is delegated to the use case via
 * {@link AuthResultMapper}, but the use cases just forward the values returned
 * by these methods.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int REFRESH_TOKEN_BYTES = 48;

    private final UserPersistencePort userPersistencePort;
    private final AuthSessionPersistencePort authSessionPersistencePort;
    private final SocialLinkPersistencePort socialLinkPersistencePort;
    private final PasswordHasher passwordHasher;
    private final AccessTokenIssuer accessTokenIssuer;
    private final SocialTokenVerifier socialTokenVerifier;
    private final AuthPolicy authPolicy;
    private final RefreshTokenStore refreshTokenStore;
    private final AuthResultMapper authResultMapper;
    private final TokenBlacklist tokenBlacklist;

    public LoginResult login(LoginCommand command) {
        log.debug("Login attempt for identity: {}", command.identity());
        IdentityUser user = validateCredentials(command.identity(), command.password());

        AuthSession session = createSession(user.getId().toString(), command.ipAddress(), command.userAgent());
        AuthSession savedSession = authSessionPersistencePort.save(session);
        String accessToken = issueAccessToken(savedSession);
        String refreshToken = issueRefreshToken(savedSession);

        log.info("User logged in: userId={}, sessionId={}", user.getId(), savedSession.getSessionId());
        return authResultMapper.toLoginResult(savedSession, accessToken, refreshToken);
    }

    public SocialLoginResult socialLogin(SocialLoginCommand command) {
        log.debug("Social login attempt: provider={}", command.provider());

        AuthProvider provider = AuthProvider.from(command.provider());
        String providerUserId = socialTokenVerifier.verifyAndExtractProviderUserId(provider, command.providerToken());

        IdentityUser user;
        boolean isNewUser = false;
        var socialLink = socialLinkPersistencePort.findByProviderAndProviderUserId(provider, providerUserId);
        if (socialLink.isPresent()) {
            user = userPersistencePort.findById(socialLink.get().userId())
                    .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
        } else {
            user = createUserForSocial(provider, providerUserId);
            IdentityUser savedUser = userPersistencePort.save(user);
            SocialLink newSocialLink = SocialLink.createNew(
                    IdGenerator.ulid(),
                    savedUser.getId().toString(),
                    provider,
                    providerUserId);
            socialLinkPersistencePort.save(newSocialLink, savedUser.getId().toString());
            user = savedUser;
            isNewUser = true;
            log.info("New user via social login: userId={}, provider={}", user.getId(), provider);
        }

        validateActiveUser(user);
        AuthSession session = createSession(user.getId().toString(), command.ipAddress(), command.userAgent());
        AuthSession savedSession = authSessionPersistencePort.save(session);
        String accessToken = issueAccessToken(savedSession);
        String refreshToken = issueRefreshToken(savedSession);

        return authResultMapper.toSocialLoginResult(savedSession, accessToken, refreshToken, isNewUser);
    }

    public List<AuthSession> listSessions(String userId) {
        validateUserExists(userId);
        return authSessionPersistencePort.findByUserId(userId);
    }

    public SocialLink linkSocial(LinkSocialCommand command) {
        IdentityUser user = validateUserExists(command.userId());
        AuthProvider provider = AuthProvider.from(command.provider());
        String providerUserId = socialTokenVerifier.verifyAndExtractProviderUserId(provider, command.providerToken());

        if (socialLinkPersistencePort.existsByProviderAndProviderUserId(provider, providerUserId)
                || socialLinkPersistencePort.findByUserIdAndProvider(command.userId(), provider).isPresent()) {
            throw new IdentityException(IdentityErrorCode.SOCIAL_LINK_EXISTS);
        }

        SocialLink domainSocialLink = SocialLink.createNew(
                IdGenerator.ulid(),
                user.getId().toString(),
                provider,
                providerUserId);
        return socialLinkPersistencePort.save(domainSocialLink, user.getId().toString());
    }

    public void unlinkSocial(UnlinkSocialCommand command) {
        IdentityUser user = validateUserExists(command.userId());
        AuthProvider provider = AuthProvider.from(command.provider());
        socialLinkPersistencePort.findByUserIdAndProvider(command.userId(), provider)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.SOCIAL_LINK_NOT_FOUND));

        // Refuse to remove the last login mechanism: user would be locked out.
        boolean hasPassword = user.getPasswordHash() != null && !user.getPasswordHash().isBlank();
        boolean hasOtherCredential = hasPassword
                || (user.getPhone() != null && !user.getPhone().isBlank())
                || (user.getEmail() != null && !user.getEmail().isBlank());
        if (!hasOtherCredential) {
            throw new IdentityException(IdentityErrorCode.SOCIAL_LINK_NOT_FOUND,
                    "Cannot unlink the only login method on this account");
        }

        socialLinkPersistencePort.deleteByUserIdAndProvider(command.userId(), provider);
    }

    public void revokeSession(RevokeSessionCommand command) {
        revokeSessionInternal(command.userId(), command.sessionId());
    }

    public void logout(LogoutCommand command) {
        revokeSessionInternal(command.userId(), command.sessionId());
        refreshTokenStore.revokeBySessionId(command.sessionId());
        // Blacklist the current access token so it's rejected immediately
        // (max 15 min TTL, auto-expires from blacklist)
        if (command.accessTokenJti() != null && !command.accessTokenJti().isBlank()) {
            tokenBlacklist.blacklist(command.accessTokenJti(), authPolicy.getAccessTokenExpiryMinutes() * 60L);
        }
    }

    public LogoutAllResult logoutAll(LogoutAllCommand command) {
        validateUserExists(command.userId());
        int revoked = 0;
        var sessions = authSessionPersistencePort.findByUserId(command.userId());
        for (AuthSession session : sessions) {
            if (AuthSessionStatus.ACTIVE.equals(session.getStatus())) {
                session.revoke();
                refreshTokenStore.revokeBySessionId(session.getSessionId());
                revoked++;
            }
        }
        authSessionPersistencePort.saveAll(sessions);
        return authResultMapper.toLogoutAllResult(revoked);
    }

    /**
     * Refresh-token rotation: lookup token, ensure session still active,
     * extend session, issue a fresh access+refresh pair, invalidate the old
     * token. Tokens are single-use so a stolen refresh token cannot be reused
     * after the legitimate user refreshes.
     */
    public RefreshAccessTokenResult refreshToken(RefreshTokenCommand command) {
        String tokenId = pickToken(command);
        if (tokenId == null) {
            throw new IdentityException(IdentityErrorCode.VERIFICATION_TOKEN_INVALID);
        }

        String sessionId = refreshTokenStore.findSessionId(tokenId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.VERIFICATION_TOKEN_INVALID));

        AuthSession session = authSessionPersistencePort.findById(sessionId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.SESSION_NOT_FOUND));
        if (!AuthSessionStatus.ACTIVE.equals(session.getStatus()) || session.isExpired()) {
            refreshTokenStore.revoke(tokenId);
            throw new IdentityException(IdentityErrorCode.VERIFICATION_TOKEN_INVALID);
        }

        // Single-use rotation: invalidate this specific refresh token.
        refreshTokenStore.revoke(tokenId);

        session.extendExpiry(LocalDateTime.now().plusDays(authPolicy.getSessionExpiresDays()));
        AuthSession refreshed = authSessionPersistencePort.save(session);

        String accessToken = issueAccessToken(refreshed);
        String newRefreshToken = issueRefreshToken(refreshed);
        return authResultMapper.toRefreshResult(refreshed, accessToken, newRefreshToken);
    }

    private static String pickToken(RefreshTokenCommand command) {
        if (command.requestRefreshToken() != null && !command.requestRefreshToken().isBlank()) {
            return command.requestRefreshToken();
        }
        if (command.cookieRefreshToken() != null && !command.cookieRefreshToken().isBlank()) {
            return command.cookieRefreshToken();
        }
        return null;
    }

    private void revokeSessionInternal(String userId, String sessionId) {
        validateUserExists(userId);
        AuthSession session = authSessionPersistencePort.findById(sessionId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.SESSION_NOT_FOUND));

        if (!session.getUserId().equals(userId)) {
            throw new IdentityException(IdentityErrorCode.SESSION_FORBIDDEN);
        }

        if (AuthSessionStatus.ACTIVE.equals(session.getStatus())) {
            session.revoke();
            authSessionPersistencePort.save(session);
            refreshTokenStore.revokeBySessionId(sessionId);
        }
    }

    private IdentityUser validateCredentials(String identity, String password) {
        IdentityUser user = userPersistencePort.findByIdentity(identity)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.INVALID_CREDENTIALS));

        validateActiveUser(user);
        if (user.isLocked()) {
            log.warn("Login attempt against locked account: {}", identity);
            throw new IdentityException(IdentityErrorCode.USER_INACTIVE,
                    "Account is temporarily locked");
        }
        if (user.getPasswordHash() == null || !passwordHasher.matches(password, user.getPasswordHash())) {
            log.warn("Invalid password attempt for identity: {}", identity);
            throw new IdentityException(IdentityErrorCode.INVALID_CREDENTIALS);
        }
        return user;
    }

    private IdentityUser validateUserExists(String userId) {
        IdentityUser user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
        validateActiveUser(user);
        return user;
    }

    private void validateActiveUser(IdentityUser user) {
        if (!user.isActive()) {
            throw new IdentityException(IdentityErrorCode.USER_INACTIVE);
        }
    }

    private IdentityUser createUserForSocial(AuthProvider provider, String providerUserId) {
        return IdentityUser.createNew(
                UserId.of(IdGenerator.ulid()),
                null,
                null,
                generateSocialUsername(provider, providerUserId));
    }

    private String generateSocialUsername(AuthProvider provider, String providerUserId) {
        String base = (provider.name().toLowerCase() + "_" + providerUserId.replace(':', '_')
                .replaceAll("[^a-zA-Z0-9_]", "")).toLowerCase();
        if (base.length() > 40) {
            base = base.substring(0, 40);
        }
        String candidate = base;
        int suffix = 1;
        while (userPersistencePort.existsByUsername(candidate)) {
            candidate = base + "_" + suffix++;
            if (candidate.length() > 50) {
                candidate = candidate.substring(0, 50);
            }
        }
        return candidate;
    }

    private AuthSession createSession(String userId, String ipAddress, String userAgent) {
        return AuthSession.createNew(
                IdGenerator.ulid(),
                userId,
                ipAddress,
                userAgent,
                LocalDateTime.now().plusDays(authPolicy.getSessionExpiresDays()));
    }

    private String issueAccessToken(AuthSession session) {
        // Load user roles to embed in JWT for microservice-ready authorization
        var user = userPersistencePort.findById(session.getUserId()).orElse(null);
        var roles = user != null
                ? user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet())
                : java.util.Set.<String>of();
        return accessTokenIssuer.issueAccessToken(
                session.getUserId(),
                session.getSessionId(),
                session.getExpiresAt(),
                roles);
    }

    private String issueRefreshToken(AuthSession session) {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        String tokenId = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Duration ttl = Duration.between(LocalDateTime.now(), session.getExpiresAt());
        if (ttl.isNegative() || ttl.isZero()) {
            ttl = Duration.ofDays(authPolicy.getSessionExpiresDays());
        }
        refreshTokenStore.store(tokenId, session.getSessionId(), ttl);
        return tokenId;
    }
}
