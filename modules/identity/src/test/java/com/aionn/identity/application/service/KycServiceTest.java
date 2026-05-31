package com.aionn.identity.application.service;

import com.aionn.identity.application.dto.kyc.result.KycVerificationSessionResult;
import com.aionn.identity.application.policy.KycPolicy;
import com.aionn.identity.application.port.out.kyc.ExternalKycVerificationPort;
import com.aionn.identity.application.port.out.kyc.KycPersistencePort;
import com.aionn.identity.application.port.out.user.UserPersistencePort;
import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.identity.domain.model.IdentityUser;
import com.aionn.identity.domain.model.KycProfile;
import com.aionn.identity.domain.valueobject.KycStatus;
import com.aionn.identity.domain.valueobject.UserRole;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KycServiceTest {

        private static final String USER_ID = "01HZ0000000000000000000001";
        private static final String KYC_ID = "01HZKYC0000000000000000001";

        @Mock
        private KycPersistencePort kycPersistencePort;
        @Mock
        private UserPersistencePort userPersistencePort;
        @Mock
        private ExternalKycVerificationPort externalKycVerificationPort;

        private IdentityUser user;

        @BeforeEach
        void setUp() {
                user = new IdentityUser(
                                USER_ID,
                                "user@example.com",
                                "+84987654321",
                                "alice",
                                "hashed",
                                "Alice",
                                null,
                                Set.of(UserRole.BUYER),
                                UserStatus.ACTIVE,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                null,
                                LocalDateTime.now());
        }

        @Test
        void createKycAutoApprovesWhenLocalDevelopmentProviderIsEnabled() {
                var service = new KycService(
                                kycPersistencePort,
                                userPersistencePort,
                                kycPolicy("local"),
                                externalKycVerificationPort);

                when(userPersistencePort.findById(USER_ID)).thenReturn(Optional.of(user));
                when(externalKycVerificationPort.createApplicant(eq(user), anyString(), eq("PASSPORT")))
                                .thenReturn(new ExternalKycVerificationPort.ExternalKycApplicant(
                                                "local",
                                                "local-applicant-1",
                                                "local-dev-kyc",
                                                "completed",
                                                "local-correlation-1"));
                when(kycPersistencePort.save(any(KycProfile.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                KycProfile result = service.createKyc(USER_ID, "PASSPORT");

                assertEquals(KycStatus.APPROVED, result.getStatus());
                assertEquals("local", result.getProvider());
                assertEquals("LOCAL", result.getDecisionAdminId());
                verify(externalKycVerificationPort).createApplicant(eq(user), anyString(), eq("PASSPORT"));
        }

        @Test
        void createKycAttachesExternalApplicantWhenSumsubIsEnabled() {
                var service = new KycService(
                                kycPersistencePort,
                                userPersistencePort,
                                kycPolicy("sumsub"),
                                externalKycVerificationPort);

                when(userPersistencePort.findById(USER_ID)).thenReturn(Optional.of(user));
                when(externalKycVerificationPort.createApplicant(eq(user), anyString(), eq("PASSPORT")))
                                .thenReturn(new ExternalKycVerificationPort.ExternalKycApplicant(
                                                "sumsub",
                                                "applicant-1",
                                                "basic-kyc",
                                                "pending",
                                                "corr-1"));
                when(kycPersistencePort.save(any(KycProfile.class))).thenAnswer(invocation -> {
                        KycProfile profile = invocation.getArgument(0);
                        return new KycProfile(
                                        profile.getKycId(),
                                        profile.getUserId(),
                                        profile.getDocType(),
                                        profile.getBlobUrl(),
                                        profile.getStatus(),
                                        profile.getProvider(),
                                        profile.getProviderApplicantId(),
                                        profile.getProviderLevelName(),
                                        profile.getProviderReviewStatus(),
                                        profile.getProviderCorrelationId(),
                                        profile.getReviewerId(),
                                        profile.getReviewNote(),
                                        profile.getDecisionAdminId(),
                                        profile.getRejectReason(),
                                        profile.getSubmittedAt(),
                                        profile.getApprovedAt(),
                                        profile.getCreatedAt());
                });

                KycProfile result = service.createKyc(USER_ID, "PASSPORT");

                assertEquals(KycStatus.SUBMITTED, result.getStatus());
                assertEquals("sumsub", result.getProvider());
                assertEquals("applicant-1", result.getProviderApplicantId());
                assertNotNull(result.getKycId());
                assertTrue(result.isManagedExternally());
                assertNotNull(result.getSubmittedAt());
        }

        @Test
        void generateVerificationSessionRejectsLocallyManagedProfile() {
                var service = new KycService(
                                kycPersistencePort,
                                userPersistencePort,
                                kycPolicy("sumsub"),
                                externalKycVerificationPort);

                KycProfile localKyc = new KycProfile(
                                KYC_ID,
                                USER_ID,
                                "PASSPORT",
                                null,
                                KycStatus.SUBMITTED,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                LocalDateTime.now(),
                                null,
                                LocalDateTime.now());

                when(userPersistencePort.findById(USER_ID)).thenReturn(Optional.of(user));
                when(kycPersistencePort.findByKycIdAndUserId(KYC_ID, USER_ID)).thenReturn(Optional.of(localKyc));

                IdentityException ex = assertThrows(
                                IdentityException.class,
                                () -> service.generateVerificationSession(USER_ID, KYC_ID));

                assertEquals(IdentityErrorCode.KYC_PROVIDER_NOT_CONFIGURED.getCode(), ex.getErrorCode());
        }

        @Test
        void generateVerificationSessionReturnsSyntheticSessionForLocalDevelopmentProvider() {
                var service = new KycService(
                                kycPersistencePort,
                                userPersistencePort,
                                kycPolicy("local"),
                                externalKycVerificationPort);

                KycProfile localKyc = new KycProfile(
                                KYC_ID,
                                USER_ID,
                                "PASSPORT",
                                null,
                                KycStatus.APPROVED,
                                "local",
                                "local-applicant-1",
                                "local-dev-kyc",
                                "completed",
                                "local-correlation-1",
                                null,
                                null,
                                "LOCAL",
                                null,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                LocalDateTime.now());

                when(userPersistencePort.findById(USER_ID)).thenReturn(Optional.of(user));
                when(kycPersistencePort.findByKycIdAndUserId(KYC_ID, USER_ID)).thenReturn(Optional.of(localKyc));
                when(externalKycVerificationPort.generateVerificationSession(user, KYC_ID, "local-applicant-1"))
                                .thenReturn(new ExternalKycVerificationPort.ExternalKycSession(
                                                "local",
                                                "local-applicant-1",
                                                "local-dev-kyc",
                                                "local-sdk-token-1",
                                                600,
                                                true));

                KycVerificationSessionResult result = service.generateVerificationSession(USER_ID, KYC_ID);

                assertEquals(KYC_ID, result.kycId());
                assertEquals("local", result.provider());
                assertEquals("local-sdk-token-1", result.sdkAccessToken());
                assertTrue(result.sandbox());
        }

        @Test
        void handleSumsubWebhookApprovesProfileAndPersistsUpdate() {
                var service = new KycService(
                                kycPersistencePort,
                                userPersistencePort,
                                kycPolicy("sumsub"),
                                externalKycVerificationPort);

                KycProfile submitted = new KycProfile(
                                KYC_ID,
                                USER_ID,
                                "PASSPORT",
                                null,
                                KycStatus.SUBMITTED,
                                "sumsub",
                                "applicant-1",
                                "basic-kyc",
                                "pending",
                                null,
                                null,
                                null,
                                null,
                                null,
                                LocalDateTime.now(),
                                null,
                                LocalDateTime.now());

                when(kycPersistencePort.findByProviderApplicantId("applicant-1")).thenReturn(Optional.of(submitted));
                when(kycPersistencePort.save(any(KycProfile.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                byte[] payload = "{}".getBytes();

                service.handleSumsubWebhook(
                                payload,
                                "digest",
                                "HMAC_SHA256_HEX",
                                "applicant-1",
                                "completed",
                                "GREEN",
                                "approved",
                                null,
                                "corr-1");

                verify(externalKycVerificationPort).verifyWebhookSignature(payload, "digest", "HMAC_SHA256_HEX");
                verify(kycPersistencePort).save(any(KycProfile.class));
                assertEquals(KycStatus.APPROVED, submitted.getStatus());
                assertEquals("SUMSUB", submitted.getDecisionAdminId());
                assertNotNull(submitted.getApprovedAt());
        }

        private static KycPolicy kycPolicy(String provider) {
                return new KycPolicy() {
                        @Override
                        public boolean isSumsubEnabled() {
                                return "sumsub".equalsIgnoreCase(provider);
                        }

                        @Override
                        public boolean isLocalDevelopmentEnabled() {
                                return "local".equalsIgnoreCase(provider);
                        }

                        @Override
                        public boolean usesManagedProvider() {
                                return isSumsubEnabled() || isLocalDevelopmentEnabled();
                        }
                };
        }
}
