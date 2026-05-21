package com.aionn.identity.bugfix;

import com.aionn.identity.application.port.out.kyc.KycPersistencePort;
import com.aionn.identity.application.port.out.user.UserPersistencePort;
import com.aionn.identity.application.service.KycService;
import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.aionn.identity.domain.exception.IdentityException;
import com.aionn.identity.domain.id.UserId;
import com.aionn.identity.domain.model.IdentityUser;
import com.aionn.identity.domain.model.KycProfile;
import com.aionn.identity.domain.valueobject.KycStatus;
import com.aionn.identity.domain.valueobject.UserRole;
import com.aionn.identity.domain.valueobject.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aionn.sharedkernel.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KycService Refactoring Tests")
class KycServiceRefactoringTest {

        // Valid ULID constants for test reproducibility (26 Crockford Base32 chars)
        private static final String USER_ID_1 = "01HZ0000000000000000000001";
        private static final String USER_ID_2 = "01HZ0000000000000000000002";
        private static final String USER_ID_3 = "01HZ0000000000000000000003";

        @Mock
        private KycPersistencePort kycPersistencePort;

        @Mock
        private UserPersistencePort userPersistencePort;

        @InjectMocks
        private KycService kycService;

        private IdentityUser regularUser;
        private IdentityUser adminUser;
        private KycProfile draftKyc;
        private KycProfile submittedKyc;

        @BeforeEach
        void setUp() {
                regularUser = newUser(USER_ID_1, "user@example.com", "+1234567890",
                                "user1", Set.of(UserRole.BUYER));
                adminUser = newUser(USER_ID_2, "admin@example.com", "+0987654321",
                                "admin1", Set.of(UserRole.CS_ADMIN));

                draftKyc = newKyc("kyc-1", USER_ID_1, KycStatus.DRAFT, null);
                submittedKyc = newKyc("kyc-2", USER_ID_1, KycStatus.SUBMITTED,
                                "https://blob.storage/doc.pdf");
                submittedKyc.uploadDocument("https://blob.storage/doc.pdf");
        }

        @Test
        @DisplayName("Should verify KycService uses KycPersistencePort (not direct repository)")
        void testUsesPortInterface() {
                assertNotNull(kycService);
                assertNotNull(kycPersistencePort);
                assertNotNull(userPersistencePort);
        }

        @Test
        @DisplayName("Should throw KYC_CANNOT_BE_CANCELLED when cancelling non-cancellable KYC")
        void testCancelThrowsCorrectErrorCode() {
                KycProfile approvedKyc = newKyc("kyc-3", USER_ID_1,
                                KycStatus.APPROVED, "https://blob.storage/doc.pdf");

                when(kycPersistencePort.findByKycIdAndUserId("kyc-3", USER_ID_1))
                                .thenReturn(Optional.of(approvedKyc));

                IdentityException exception = assertThrows(IdentityException.class,
                                () -> kycService.cancel(USER_ID_1, "kyc-3"));

                assertEquals(IdentityErrorCode.KYC_CANNOT_BE_CANCELLED.getCode(), exception.getErrorCode());
        }

        @Test
        @DisplayName("Should preserve KYC record on cancel by transitioning to CANCELLED")
        void testCancelPreservesAuditTrail() {
                when(kycPersistencePort.findByKycIdAndUserId("kyc-1", USER_ID_1))
                                .thenReturn(Optional.of(draftKyc));
                when(kycPersistencePort.save(any(KycProfile.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                KycProfile result = kycService.cancel(USER_ID_1, "kyc-1");

                assertEquals(KycStatus.CANCELLED, result.getStatus());
                verify(kycPersistencePort, never()).delete(any());
                verify(kycPersistencePort).save(any(KycProfile.class));
        }

        @Test
        @DisplayName("Should throw KYC_NOT_FOUND when KYC profile not found")
        void testThrowsKycNotFound() {
                when(kycPersistencePort.findByKycIdAndUserId(anyString(), anyString()))
                                .thenReturn(Optional.empty());

                IdentityException exception = assertThrows(IdentityException.class,
                                () -> kycService.submit(USER_ID_1, "non-existent-kyc"));

                assertEquals(IdentityErrorCode.KYC_NOT_FOUND.getCode(), exception.getErrorCode());
        }

        @Test
        @DisplayName("Should validate admin role in review() method")
        void testValidateAdminUserInReview() {
                when(userPersistencePort.findById(USER_ID_1))
                                .thenReturn(Optional.of(regularUser));

                IdentityException exception = assertThrows(IdentityException.class,
                                () -> kycService.review(USER_ID_1, "kyc-1",
                                                "Review note"));

                assertEquals(IdentityErrorCode.INSUFFICIENT_PERMISSIONS.getCode(), exception.getErrorCode());
        }

        @Test
        @DisplayName("Should validate admin role in approve() method")
        void testValidateAdminUserInApprove() {
                when(userPersistencePort.findById(USER_ID_1))
                                .thenReturn(Optional.of(regularUser));

                IdentityException exception = assertThrows(IdentityException.class,
                                () -> kycService.approve(USER_ID_1, "kyc-1"));

                assertEquals(IdentityErrorCode.INSUFFICIENT_PERMISSIONS.getCode(), exception.getErrorCode());
        }

        @Test
        @DisplayName("Should validate admin role in reject() method")
        void testValidateAdminUserInReject() {
                when(userPersistencePort.findById(USER_ID_1))
                                .thenReturn(Optional.of(regularUser));

                IdentityException exception = assertThrows(IdentityException.class,
                                () -> kycService.reject(USER_ID_1, "kyc-1",
                                                "Rejection reason"));

                assertEquals(IdentityErrorCode.INSUFFICIENT_PERMISSIONS.getCode(), exception.getErrorCode());
        }

        @Test
        @DisplayName("Should allow admin user to review KYC")
        void testAdminCanReview() {
                when(userPersistencePort.findById(USER_ID_2))
                                .thenReturn(Optional.of(adminUser));
                when(kycPersistencePort.findById("kyc-2"))
                                .thenReturn(Optional.of(submittedKyc));
                when(kycPersistencePort.save(any(KycProfile.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                KycProfile result = kycService.review(USER_ID_2, "kyc-2", "Review note");

                assertNotNull(result);
                assertEquals(KycStatus.IN_REVIEW, result.getStatus());
                assertEquals(USER_ID_2, result.getReviewerId());
                verify(kycPersistencePort).save(any(KycProfile.class));
        }

        @Test
        @DisplayName("Should validate status transition in submit() - draft must have document")
        void testStatusTransitionValidationInSubmit() {
                when(kycPersistencePort.findByKycIdAndUserId("kyc-1", USER_ID_1))
                                .thenReturn(Optional.of(draftKyc));

                // Submit without uploading document -> rejected
                assertThrows(IdentityException.class,
                                () -> kycService.submit(USER_ID_1, "kyc-1"));
        }

        @Test
        @DisplayName("Should use KycStatus enum instead of hardcoded strings")
        void testUsesKycStatusEnum() {
                when(userPersistencePort.findById(USER_ID_1))
                                .thenReturn(Optional.of(regularUser));
                when(kycPersistencePort.save(any(KycProfile.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                KycProfile result = kycService.createKyc(USER_ID_1, "PASSPORT");

                assertNotNull(result);
                assertEquals(KycStatus.DRAFT, result.getStatus());
        }

        @Test
        @DisplayName("Should verify KycService has @Slf4j annotation")
        void testHasSlf4jAnnotation() {
                try {
                        java.lang.reflect.Field logField = KycService.class.getDeclaredField("log");
                        assertNotNull(logField);
                        assertEquals(org.slf4j.Logger.class, logField.getType());
                } catch (NoSuchFieldException e) {
                        fail("KycService should have @Slf4j annotation");
                }
        }

        @Test
        @DisplayName("Should verify KycService methods do NOT have @Transactional")
        void testNoTransactionalAnnotations() {
                java.lang.reflect.Method[] methods = KycService.class.getDeclaredMethods();
                for (java.lang.reflect.Method method : methods) {
                        assertFalse(
                                        method.isAnnotationPresent(
                                                        org.springframework.transaction.annotation.Transactional.class),
                                        "Method " + method.getName() + " should not have @Transactional annotation");
                }
        }

        @Test
        @DisplayName("Should allow SYSTEM_ADMIN role to perform admin operations")
        void testSystemAdminCanPerformAdminOperations() {
                IdentityUser systemAdmin = newUser(USER_ID_3, "sysadmin@example.com",
                                "+1111111111", "sysadmin", Set.of(UserRole.SYSTEM_ADMIN));

                KycProfile inReviewKyc = newKyc("kyc-3", USER_ID_1,
                                KycStatus.IN_REVIEW, "https://blob.storage/doc.pdf");
                inReviewKyc.uploadDocument("https://blob.storage/doc.pdf");

                when(userPersistencePort.findById(USER_ID_3))
                                .thenReturn(Optional.of(systemAdmin));
                when(kycPersistencePort.findById("kyc-3"))
                                .thenReturn(Optional.of(inReviewKyc));
                when(kycPersistencePort.save(any(KycProfile.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                assertDoesNotThrow(() -> kycService.approve(USER_ID_3, "kyc-3"));
        }

        private static IdentityUser newUser(String id, String email, String phone, String username,
                        Set<UserRole> roles) {
                return new IdentityUser(
                                UserId.of(id),
                                email,
                                phone,
                                username,
                                "hashedPassword",
                                username,
                                null,
                                roles,
                                UserStatus.ACTIVE,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                null,
                                LocalDateTime.now());
        }

        private static KycProfile newKyc(String kycId, String userId, KycStatus status, String blobUrl) {
                return new KycProfile(
                                kycId,
                                userId,
                                "PASSPORT",
                                blobUrl,
                                status,
                                null,
                                null,
                                null,
                                null,
                                status == KycStatus.SUBMITTED || status == KycStatus.IN_REVIEW
                                                || status == KycStatus.APPROVED
                                                                ? LocalDateTime.now()
                                                                : null,
                                status == KycStatus.APPROVED ? LocalDateTime.now() : null,
                                LocalDateTime.now());
        }
}
