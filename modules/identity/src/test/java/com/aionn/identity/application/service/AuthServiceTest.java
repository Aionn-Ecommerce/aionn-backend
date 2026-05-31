package com.aionn.identity.application.service;

import com.aionn.identity.application.dto.auth.command.LoginCommand;
import com.aionn.identity.application.mapper.AuthResultMapper;
import com.aionn.identity.application.policy.AuthPolicy;
import com.aionn.identity.application.port.out.auth.AccessTokenIssuerPort;
import com.aionn.identity.application.port.out.auth.AuthSessionPersistencePort;
import com.aionn.identity.application.port.out.auth.RefreshTokenStorePort;
import com.aionn.identity.application.port.out.auth.TokenBlacklistPort;
import com.aionn.identity.application.port.out.security.MfaPersistencePort;
import com.aionn.identity.application.port.out.security.PasswordHasherPort;
import com.aionn.identity.application.port.out.security.TotpManagerPort;
import com.aionn.identity.application.port.out.security.UserSecurityPort;
import com.aionn.identity.application.port.out.social.SocialLinkPersistencePort;
import com.aionn.identity.application.port.out.social.SocialTokenVerifierPort;
import com.aionn.identity.application.port.out.user.UserPersistencePort;
import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.identity.domain.model.IdentityUser;
import com.aionn.identity.domain.valueobject.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

        @Mock
        private UserPersistencePort userPersistencePort;
        @Mock
        private UserSecurityPort userSecurityPort;
        @Mock
        private AuthSessionPersistencePort authSessionPersistencePort;
        @Mock
        private SocialLinkPersistencePort socialLinkPersistencePort;
        @Mock
        private MfaPersistencePort mfaPersistencePort;
        @Mock
        private PasswordHasherPort passwordHasher;
        @Mock
        private TotpManagerPort totpManager;
        @Mock
        private AccessTokenIssuerPort accessTokenIssuer;
        @Mock
        private SocialTokenVerifierPort socialTokenVerifier;
        @Mock
        private AuthPolicy authPolicy;
        @Mock
        private RefreshTokenStorePort refreshTokenStore;
        @Mock
        private AuthResultMapper authResultMapper;
        @Mock
        private TokenBlacklistPort tokenBlacklist;

        private AuthService authService;

        @BeforeEach
        void setUp() {
                authService = new AuthService(
                                userPersistencePort,
                                userSecurityPort,
                                authSessionPersistencePort,
                                socialLinkPersistencePort,
                                mfaPersistencePort,
                                passwordHasher,
                                totpManager,
                                accessTokenIssuer,
                                socialTokenVerifier,
                                authPolicy,
                                refreshTokenStore,
                                authResultMapper,
                                tokenBlacklist);
        }

        @Test
        void loginRequiresMfaCodeWhenMfaEnabled() {
                var user = new IdentityUser(
                                "01ARZ3NDEKTSV4RRFFQ69G5FAV",
                                "a@example.com",
                                null,
                                "alice",
                                "hash",
                                null,
                                null,
                                Set.of(),
                                UserStatus.ACTIVE,
                                null,
                                null,
                                null,
                                LocalDateTime.now());
                var securityData = new UserSecurityPort.UserSecurityData(
                                user.getUserId(),
                                "hash",
                                UserStatus.ACTIVE,
                                true,
                                "JBSWY3DPEHPK3PXP",
                                null,
                                0);

                when(userSecurityPort.findByIdentity("alice")).thenReturn(Optional.of(securityData));
                when(passwordHasher.matches("secret", "hash")).thenReturn(true);
                when(userPersistencePort.findById(user.getUserId())).thenReturn(Optional.of(user));
                when(userSecurityPort.findById(user.getUserId())).thenReturn(Optional.of(securityData));
                when(authPolicy.getMaxFailedLoginAttempts()).thenReturn(5);

                var ex = assertThrows(IdentityException.class,
                                () -> authService.login(new LoginCommand("alice", "secret", null, "1.1.1.1", "ua")));

                assertEquals(IdentityErrorCode.OTP_REQUIRED.getCode(), ex.getErrorCode());
                verify(userSecurityPort).recordFailedLoginAttempt(eq(user.getUserId()), eq(1), eq(null));
        }

        @Test
        void invalidPasswordLocksAccountAtThreshold() {
                var securityData = new UserSecurityPort.UserSecurityData(
                                "01HLOCKUSER0000000000000000",
                                "hash",
                                UserStatus.ACTIVE,
                                false,
                                null,
                                null,
                                4);

                when(userSecurityPort.findByIdentity("alice")).thenReturn(Optional.of(securityData));
                when(passwordHasher.matches("bad-password", "hash")).thenReturn(false);
                when(authPolicy.getMaxFailedLoginAttempts()).thenReturn(5);
                when(authPolicy.getLockoutMinutes()).thenReturn(15);

                var ex = assertThrows(IdentityException.class,
                                () -> authService.login(
                                                new LoginCommand("alice", "bad-password", null, "1.1.1.1", "ua")));

                assertEquals(IdentityErrorCode.INVALID_CREDENTIALS.getCode(), ex.getErrorCode());
                verify(userSecurityPort).recordFailedLoginAttempt(eq(securityData.userId()), eq(5),
                                any(LocalDateTime.class));
                verify(userPersistencePort, never()).findById(any());
        }
}
