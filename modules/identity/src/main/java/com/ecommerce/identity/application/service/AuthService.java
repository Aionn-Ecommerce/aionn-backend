package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.dto.auth.command.LinkSocialCommand;
import com.ecommerce.identity.application.dto.auth.command.LoginCommand;
import com.ecommerce.identity.application.dto.auth.command.LogoutAllCommand;
import com.ecommerce.identity.application.dto.auth.command.LogoutCommand;
import com.ecommerce.identity.application.dto.auth.command.RefreshTokenCommand;
import com.ecommerce.identity.application.dto.auth.command.RevokeSessionCommand;
import com.ecommerce.identity.application.dto.auth.command.SocialLoginCommand;
import com.ecommerce.identity.application.dto.auth.command.UnlinkSocialCommand;
import com.ecommerce.identity.application.mapper.AuthResultMapper;
import com.ecommerce.identity.application.dto.auth.result.LoginResult;
import com.ecommerce.identity.application.dto.auth.result.LogoutAllResult;
import com.ecommerce.identity.application.dto.auth.result.SocialLoginResult;
import com.ecommerce.identity.application.port.out.auth.AccessTokenIssuer;
import com.ecommerce.identity.application.port.out.auth.AuthPolicy;
import com.ecommerce.identity.application.port.out.auth.AuthSessionPersistencePort;
import com.ecommerce.identity.application.port.out.auth.SocialTokenVerifier;
import com.ecommerce.identity.application.port.out.security.PasswordHasher;
import com.ecommerce.identity.application.port.out.social.SocialLinkPersistencePort;
import com.ecommerce.identity.application.port.out.user.UserPersistencePort;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.id.UserId;
import com.ecommerce.identity.domain.model.AuthSession;
import com.ecommerce.identity.domain.model.IdentityUser;
import com.ecommerce.identity.domain.model.SocialLink;
import com.ecommerce.identity.domain.valueobject.AuthProvider;
import com.ecommerce.identity.domain.valueobject.AuthSessionStatus;
import com.ecommerce.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsible for authentication operations including login, social
 * authentication,
 * session management, and social account linking.
 * 
 * <p>
 * This service orchestrates the authentication flow by:
 * <ul>
 * <li>Validating user credentials and account status</li>
 * <li>Creating and managing authentication sessions</li>
 * <li>Issuing access tokens for authenticated sessions</li>
 * <li>Handling social provider authentication and account linking</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserPersistencePort userPersistencePort;
    private final AuthSessionPersistencePort authSessionPersistencePort;
    private final SocialLinkPersistencePort socialLinkPersistencePort;
    private final PasswordHasher passwordHasher;
    private final AccessTokenIssuer accessTokenIssuer;
    private final SocialTokenVerifier socialTokenVerifier;
    private final AuthPolicy authPolicy;
    private final AuthResultMapper authResultMapper;

    /**
     * Authenticates a user with username/email and password credentials.
     * 
     * <p>
     * Authentication flow:
     * <ol>
     * <li>Validate user credentials (username/email and password)</li>
     * <li>Verify user account is active</li>
     * <li>Create a new authentication session</li>
     * <li>Issue an access token for the session</li>
     * </ol>
     * 
     * @param command the login command containing identity (username/email),
     *                password, IP address, and user agent
     * @return LoginResult containing session details and access token
     * @throws IdentityException with INVALID_CREDENTIALS if credentials are invalid
     * @throws IdentityException with USER_INACTIVE if user account is not active
     */
    public LoginResult login(LoginCommand command) {
        log.debug("Login attempt for identity: {}", command.identity());

        IdentityUser user = validateCredentials(command.identity(), command.password());

        AuthSession session = createSession(user.getId().toString(), command.ipAddress(), command.userAgent());
        AuthSession savedSession = authSessionPersistencePort.save(session);
        String accessToken = issueAccessToken(savedSession);

        log.info("User logged in successfully: userId={}, sessionId={}", user.getId(), savedSession.getSessionId());
        return authResultMapper.toLoginResult(savedSession, accessToken);
    }

    public SocialLoginResult socialLogin(SocialLoginCommand command) {
        log.debug("Social login attempt with provider: {}", command.provider());

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
            log.info("New user created via social login: userId={}, provider={}", user.getId(), provider);
        }

        validateActiveUser(user);
        AuthSession session = createSession(user.getId().toString(), command.ipAddress(), command.userAgent());
        AuthSession savedSession = authSessionPersistencePort.save(session);
        String accessToken = issueAccessToken(savedSession);

        log.info("User logged in via social provider: userId={}, provider={}, sessionId={}, isNewUser={}",
                user.getId(), provider, savedSession.getSessionId(), isNewUser);
        return authResultMapper.toSocialLoginResult(savedSession, accessToken, isNewUser);
    }

    public List<AuthSession> listSessions(String userId) {
        validateUserExists(userId);
        return authSessionPersistencePort.findByUserId(userId);
    }

    public SocialLink linkSocial(LinkSocialCommand command) {
        log.debug("Linking social account for userId: {}, provider: {}", command.userId(), command.provider());

        IdentityUser user = validateUserExists(command.userId());
        AuthProvider provider = AuthProvider.from(command.provider());
        String providerUserId = socialTokenVerifier.verifyAndExtractProviderUserId(provider, command.providerToken());

        if (socialLinkPersistencePort.existsByProviderAndProviderUserId(provider, providerUserId)) {
            throw new IdentityException(IdentityErrorCode.SOCIAL_LINK_EXISTS);
        }
        if (socialLinkPersistencePort.findByUserIdAndProvider(command.userId(), provider).isPresent()) {
            throw new IdentityException(IdentityErrorCode.SOCIAL_LINK_EXISTS);
        }

        SocialLink domainSocialLink = SocialLink.createNew(
                IdGenerator.ulid(),
                user.getId().toString(),
                provider,
                providerUserId);
        SocialLink savedLink = socialLinkPersistencePort.save(domainSocialLink, user.getId().toString());

        log.info("Social account linked: userId={}, provider={}", user.getId(), provider);
        return savedLink;
    }

    public void unlinkSocial(UnlinkSocialCommand command) {
        log.debug("Unlinking social account for userId: {}, provider: {}", command.userId(), command.provider());

        validateUserExists(command.userId());
        AuthProvider provider = AuthProvider.from(command.provider());
        socialLinkPersistencePort.findByUserIdAndProvider(command.userId(), provider)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.SOCIAL_LINK_NOT_FOUND));
        socialLinkPersistencePort.deleteByUserIdAndProvider(command.userId(), provider);

        log.info("Social account unlinked: userId={}, provider={}", command.userId(), provider);
    }

    public void revokeSession(RevokeSessionCommand command) {
        revokeSessionInternal(command.userId(), command.sessionId());
    }

    public void logout(LogoutCommand command) {
        log.debug("Logout request for userId: {}, sessionId: {}", command.userId(), command.sessionId());
        revokeSessionInternal(command.userId(), command.sessionId());
        log.info("User logged out: userId={}, sessionId={}", command.userId(), command.sessionId());
    }

    public LogoutAllResult logoutAll(LogoutAllCommand command) {
        log.debug("Logout all sessions for userId: {}", command.userId());

        validateUserExists(command.userId());
        int revoked = 0;
        var sessions = authSessionPersistencePort.findByUserId(command.userId());
        for (AuthSession session : sessions) {
            if (AuthSessionStatus.ACTIVE.equals(session.getStatus())) {
                session.revoke();
                revoked++;
            }
        }
        authSessionPersistencePort.saveAll(sessions);

        log.info("All sessions logged out: userId={}, revokedCount={}", command.userId(), revoked);
        return authResultMapper.toLogoutAllResult(revoked);
    }

    public void refreshToken(RefreshTokenCommand command) {
        throw new UnsupportedOperationException("Refresh token logic is not implemented yet.");
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
        }
    }

    /**
     * Validates user credentials (identity and password).
     * 
     * @param identity the username or email
     * @param password the password to validate
     * @return the validated IdentityUser
     * @throws IdentityException with INVALID_CREDENTIALS if user not found or
     *                           password doesn't match
     * @throws IdentityException with USER_INACTIVE if user account is not active
     */
    private IdentityUser validateCredentials(String identity, String password) {
        IdentityUser user = userPersistencePort.findByIdentity(identity)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.INVALID_CREDENTIALS));

        validateActiveUser(user);

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

    /**
     * Creates a new authentication session for the user.
     * 
     * @param userId    the user ID
     * @param ipAddress the IP address of the client
     * @param userAgent the user agent string of the client
     * @return a new AuthSession with expiration set according to policy
     */
    private AuthSession createSession(String userId, String ipAddress, String userAgent) {
        return AuthSession.createNew(
                IdGenerator.ulid(),
                userId,
                ipAddress,
                userAgent,
                LocalDateTime.now().plusDays(authPolicy.getSessionExpiresDays()));
    }

    /**
     * Issues an access token for the authenticated session.
     * 
     * @param session the authenticated session
     * @return the access token string
     */
    private String issueAccessToken(AuthSession session) {
        return accessTokenIssuer.issueAccessToken(
                session.getUserId(),
                session.getSessionId(),
                session.getExpiresAt());
    }
}
