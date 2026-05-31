package com.aionn.identity.application.service;

import com.aionn.identity.application.dto.user.view.UserProfileView;
import com.aionn.identity.application.mapper.UserResultMapper;
import com.aionn.identity.application.policy.AccountManagementPolicy;
import com.aionn.identity.application.port.out.auth.AuthSessionPersistencePort;
import com.aionn.identity.application.port.out.auth.RefreshTokenStorePort;
import com.aionn.identity.application.port.out.notification.IdentityNotificationDispatcherPort;
import com.aionn.identity.application.port.out.user.UserOtpChallengeStorePort;
import com.aionn.identity.domain.valueobject.UserOtpPurpose;
import com.aionn.identity.application.port.out.user.UserPersistencePort;
import com.aionn.identity.application.port.out.user.AccountDeletionPort;
import com.aionn.identity.application.port.out.user.DataExportPort;
import com.aionn.identity.application.port.out.user.UserOtpChallengeStorePort.UserOtpChallenge;
import com.aionn.identity.domain.model.AuthSession;
import com.aionn.identity.domain.model.IdentityUser;
import com.aionn.identity.domain.valueobject.AuthSessionStatus;
import com.aionn.identity.domain.valueobject.OtpChannel;
import com.aionn.identity.domain.valueobject.UserRole;
import com.aionn.identity.domain.valueobject.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountManagementServiceTest {

        private static final String USER_ID = "01ARZ3NDEKTSV4RRFFQ69G5FAV";

        @Mock
        private UserPersistencePort userPersistencePort;
        @Mock
        private IdentityNotificationDispatcherPort notificationDispatcher;
        @Mock
        private UserOtpChallengeStorePort userOtpChallengeStore;
        @Mock
        private AccountDeletionPort accountDeletionPort;
        @Mock
        private DataExportPort dataExportPort;
        @Mock
        private AuthSessionPersistencePort authSessionPersistencePort;
        @Mock
        private RefreshTokenStorePort refreshTokenStore;
        @Mock
        private UserResultMapper userResultMapper;

        private AccountManagementService accountManagementService;

        @BeforeEach
        void setUp() {
                accountManagementService = new AccountManagementService(
                                userPersistencePort,
                                notificationDispatcher,
                                userOtpChallengeStore,
                                accountDeletionPort,
                                dataExportPort,
                                authSessionPersistencePort,
                                refreshTokenStore,
                                accountManagementPolicy(300, 5, 30),
                                userResultMapper);
        }

        private static AccountManagementPolicy accountManagementPolicy(
                        int otpExpirySeconds, int otpMaxAttempts, int deletionGraceDays) {
                return new AccountManagementPolicy() {
                        @Override
                        public int getOtpExpirySeconds() {
                                return otpExpirySeconds;
                        }

                        @Override
                        public int getOtpMaxAttempts() {
                                return otpMaxAttempts;
                        }

                        @Override
                        public int getDeletionGraceDays() {
                                return deletionGraceDays;
                        }
                };
        }

        @Test
        void sendVerifyPrimaryEmailOtpStoresChallengeAndSendsEmail() {
                IdentityUser user = activeUser(USER_ID, "old@example.com", "+84912345678");
                when(userPersistencePort.findById(USER_ID)).thenReturn(Optional.of(user));

                accountManagementService.sendVerifyPrimaryEmailOtp(USER_ID);

                ArgumentCaptor<UserOtpChallenge> challengeCaptor = ArgumentCaptor.forClass(UserOtpChallenge.class);
                verify(userOtpChallengeStore).save(challengeCaptor.capture());
                UserOtpChallenge challenge = challengeCaptor.getValue();

                assertEquals(USER_ID, challenge.userId());
                assertEquals(UserOtpPurpose.VERIFY_PRIMARY_EMAIL, challenge.purpose());
                assertEquals(OtpChannel.EMAIL, challenge.channel());
                assertEquals("old@example.com", challenge.target());
                assertNull(challenge.pendingValue());
                assertNotNull(challenge.otpCode());
                verify(notificationDispatcher).sendEmailOtp("old@example.com", challenge.otpCode());
        }

        @Test
        void confirmVerifyPrimaryEmailOtpMarksUserVerifiedAndClearsChallenge() {
                IdentityUser user = activeUser(USER_ID, "old@example.com", "+84912345678");
                when(userPersistencePort.findById(USER_ID)).thenReturn(Optional.of(user));
                when(userOtpChallengeStore.find(USER_ID, UserOtpPurpose.VERIFY_PRIMARY_EMAIL))
                                .thenReturn(Optional.of(new UserOtpChallenge(
                                                USER_ID,
                                                UserOtpPurpose.VERIFY_PRIMARY_EMAIL,
                                                OtpChannel.EMAIL,
                                                "old@example.com",
                                                "123456",
                                                null,
                                                LocalDateTime.now().plusMinutes(5),
                                                0)));

                accountManagementService.confirmVerifyPrimaryEmailOtp(USER_ID, "123456");

                ArgumentCaptor<IdentityUser> userCaptor = ArgumentCaptor.forClass(IdentityUser.class);
                verify(userPersistencePort).save(userCaptor.capture());
                assertNotNull(userCaptor.getValue().getEmailVerifiedAt());
                verify(userOtpChallengeStore).delete(USER_ID, UserOtpPurpose.VERIFY_PRIMARY_EMAIL);
        }

        @Test
        void confirmEmailChangeUpdatesEmailRevokesSessionsAndNotifiesPreviousEmail() {
                IdentityUser user = activeUser(USER_ID, "old@example.com", "+84912345678");
                when(userPersistencePort.findById(USER_ID)).thenReturn(Optional.of(user));
                when(userOtpChallengeStore.find(USER_ID, UserOtpPurpose.CHANGE_EMAIL))
                                .thenReturn(Optional.of(new UserOtpChallenge(
                                                USER_ID,
                                                UserOtpPurpose.CHANGE_EMAIL,
                                                OtpChannel.EMAIL,
                                                "new@example.com",
                                                "654321",
                                                "new@example.com",
                                                LocalDateTime.now().plusMinutes(5),
                                                0)));
                when(userPersistencePort.findByIdentity("new@example.com")).thenReturn(Optional.empty());
                when(userPersistencePort.save(any(IdentityUser.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));
                when(userResultMapper.toUserProfileView(any(IdentityUser.class)))
                                .thenAnswer(invocation -> toProfileView(invocation.getArgument(0)));

                AuthSession activeSession = new AuthSession(
                                "session-1",
                                USER_ID,
                                "127.0.0.1",
                                "JUnit",
                                AuthSessionStatus.ACTIVE,
                                LocalDateTime.now().minusDays(1),
                                LocalDateTime.now().minusHours(1),
                                LocalDateTime.now().plusDays(1));
                AuthSession revokedSession = new AuthSession(
                                "session-2",
                                USER_ID,
                                "127.0.0.1",
                                "JUnit",
                                AuthSessionStatus.REVOKED,
                                LocalDateTime.now().minusDays(1),
                                LocalDateTime.now().minusHours(1),
                                LocalDateTime.now().plusDays(1));
                when(authSessionPersistencePort.findByUserId(USER_ID))
                                .thenReturn(List.of(activeSession, revokedSession));

                UserProfileView profile = accountManagementService.confirmEmailChange(USER_ID, "654321");

                assertEquals("new@example.com", profile.email());
                verify(userOtpChallengeStore).delete(USER_ID, UserOtpPurpose.CHANGE_EMAIL);
                verify(refreshTokenStore).revokeBySessionId("session-1");
                verify(refreshTokenStore, never()).revokeBySessionId("session-2");
                verify(authSessionPersistencePort).saveAll(List.of(activeSession, revokedSession));
                verify(notificationDispatcher).sendEmailChanged(USER_ID, "old@example.com", "new@example.com");
        }

        private IdentityUser activeUser(String userId, String email, String phone) {
                return new IdentityUser(
                                userId,
                                email,
                                phone,
                                "alice",
                                "hash",
                                "Alice",
                                null,
                                Set.of(UserRole.BUYER),
                                UserStatus.ACTIVE,
                                null,
                                null,
                                null,
                                LocalDateTime.now().minusDays(10));
        }

        private UserProfileView toProfileView(IdentityUser user) {
                return new UserProfileView(
                                user.getUserId(),
                                user.getEmail(),
                                user.getPhone(),
                                user.getUsername(),
                                user.getDisplayName(),
                                user.getAvatarUrl(),
                                user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()),
                                user.getStatus().name(),
                                user.getEmailVerifiedAt(),
                                user.getPhoneVerifiedAt(),
                                user.getCreatedAt());
        }
}
