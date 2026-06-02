package com.aionn.identity.application.service;

import com.aionn.identity.application.port.out.auth.AuthSessionPersistencePort;
import com.aionn.identity.application.port.out.auth.RefreshTokenStorePort;
import com.aionn.identity.application.port.out.integration.IdentityIntegrationEventPublisherPort;
import com.aionn.identity.application.port.out.observability.IdentityMetricsPort;
import com.aionn.sharedkernel.integration.port.notification.IdentityNotificationDispatcherPort;
import com.aionn.identity.application.policy.AuthPolicy;
import com.aionn.identity.application.port.out.security.PasswordHasherPort;
import com.aionn.identity.application.port.out.security.PasswordResetPort;
import com.aionn.identity.application.port.out.security.SecurityAuditPort;
import com.aionn.identity.application.port.out.security.UserSecurityPort;
import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

        @Mock
        private UserSecurityPort userSecurityPort;
        @Mock
        private PasswordResetPort passwordResetPort;
        @Mock
        private SecurityAuditPort securityAuditPort;
        @Mock
        private PasswordHasherPort passwordHasher;
        @Mock
        private AuthSessionPersistencePort authSessionPersistencePort;
        @Mock
        private RefreshTokenStorePort refreshTokenStore;
        @Mock
        private IdentityNotificationDispatcherPort notificationDispatcher;
        @Mock
        private IdentityIntegrationEventPublisherPort integrationEventPublisher;
        @Mock
        private IdentityMetricsPort identityMetrics;
        @Mock
        private AuthPolicy authPolicy;

        private PasswordResetService passwordResetService;

        @BeforeEach
        void setUp() {
                passwordResetService = new PasswordResetService(
                                userSecurityPort,
                                passwordResetPort,
                                securityAuditPort,
                                passwordHasher,
                                authSessionPersistencePort,
                                refreshTokenStore,
                                notificationDispatcher,
                                integrationEventPublisher,
                                identityMetrics,
                                authPolicy);
        }

        @Test
        void completePasswordResetRejectsExpiredConsumedToken() {
                when(passwordResetPort.consumePasswordResetToken("token")).thenReturn(Optional.of(
                                new PasswordResetPort.PasswordResetTokenData("user-1",
                                                LocalDateTime.now().minusMinutes(1))));

                var ex = assertThrows(IdentityException.class,
                                () -> passwordResetService.completePasswordReset("token", "Password123", "1.1.1.1"));

                assertEquals(IdentityErrorCode.OTP_EXPIRED.getCode(), ex.getErrorCode());
                verify(passwordResetPort, never()).updatePassword(anyString(), anyString());
        }

        @Test
        void completePasswordResetUsesTokenOnlyOnce() {
                when(passwordResetPort.consumePasswordResetToken("token"))
                                .thenReturn(Optional.of(new PasswordResetPort.PasswordResetTokenData(
                                                "user-1",
                                                LocalDateTime.now().plusMinutes(10))))
                                .thenReturn(Optional.empty());
                when(passwordHasher.hash("Password123")).thenReturn("hashed");
                when(authSessionPersistencePort.findByUserId("user-1")).thenReturn(List.of());

                passwordResetService.completePasswordReset("token", "Password123", "1.1.1.1");

                var ex = assertThrows(IdentityException.class,
                                () -> passwordResetService.completePasswordReset("token", "Password123", "1.1.1.1"));

                assertEquals(IdentityErrorCode.VERIFICATION_TOKEN_INVALID.getCode(), ex.getErrorCode());
                verify(passwordResetPort).updatePassword("user-1", "hashed");
        }
}
