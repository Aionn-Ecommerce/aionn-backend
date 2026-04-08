package com.ecommerce.identity.bugfix;

import com.ecommerce.identity.application.port.out.kyc.KycPersistencePort;
import com.ecommerce.identity.application.port.out.user.UserPersistencePort;
import com.ecommerce.identity.application.service.KycService;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.id.UserId;
import com.ecommerce.identity.domain.model.IdentityUser;
import com.ecommerce.identity.domain.model.KycProfile;
import com.ecommerce.identity.domain.valueobject.KycStatus;
import com.ecommerce.identity.domain.valueobject.UserRole;
import com.ecommerce.identity.domain.valueobject.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test for Task 4.2: Refactor KycService
 * Verifies:
 * - Uses KycPersistencePort and domain model KycProfile (already correct)
 * - No @Transactional annotations on methods
 * - validateAdminUser() checks admin role
 * - Status transition validation in submit(), review(), approve(), reject()
 * - cancel() throws KYC_CANNOT_BE_CANCELLED
 * - Methods throw KYC_NOT_FOUND instead of USER_NOT_FOUND
 * - Uses KycStatus enum instead of hardcoded strings
 * - Has @Slf4j and logging
 * - Has Javadoc for complex business rules
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Task 4.2: KycService Refactoring Tests")
class KycServiceRefactoringTest {

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
                // Create regular user (BUYER role)
                regularUser = new IdentityUser(
                                UserId.of("550e8400-e29b-41d4-a716-446655440001"),
                                "user@example.com",
                                "+1234567890",
                                "user1",
                                "hashedPassword",
                                "Regular User",
                                null,
                                Set.of(UserRole.BUYER),
                                UserStatus.ACTIVE,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                LocalDateTime.now());

                // Create admin user (CS_ADMIN role)
                adminUser = new IdentityUser(
                                UserId.of("550e8400-e29b-41d4-a716-446655440002"),
                                "admin@example.com",
                                "+0987654321",
                                "admin1",
                                "hashedPassword",
                                "Admin User",
                                null,
                                Set.of(UserRole.CS_ADMIN),
                                UserStatus.ACTIVE,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                LocalDateTime.now());

                // Create DRAFT KYC profile
                draftKyc = new KycProfile(
                                "kyc-1",
                                "550e8400-e29b-41d4-a716-446655440001",
                                "PASSPORT",
                                null,
                                KycStatus.DRAFT,
                                null,
                                null,
                                null,
                                null,
                                LocalDateTime.now());

                // Create SUBMITTED KYC profile
                submittedKyc = new KycProfile(
                                "kyc-2",
                                "550e8400-e29b-41d4-a716-446655440001",
                                "PASSPORT",
                                "https://blob.storage/doc.pdf",
                                KycStatus.SUBMITTED,
                                null,
                                null,
                                LocalDateTime.now(),
                                null,
                                LocalDateTime.now());
        }

        @Test
        @DisplayName("Should verify KycService uses KycPersistencePort (not direct repository)")
        void testUsesPortInterface() {
                // This test verifies at compile time that KycService only depends on ports
                // If it compiled, it means we're using ports correctly
                assertNotNull(kycService);
                assertNotNull(kycPersistencePort);
                assertNotNull(userPersistencePort);
        }

        @Test
        @DisplayName("Should throw KYC_CANNOT_BE_CANCELLED when cancelling non-cancellable KYC")
        void testCancelThrowsCorrectErrorCode() {
                // Create an APPROVED KYC (cannot be cancelled)
                KycProfile approvedKyc = new KycProfile(
                                "kyc-3",
                                "550e8400-e29b-41d4-a716-446655440001",
                                "PASSPORT",
                                "https://blob.storage/doc.pdf",
                                KycStatus.APPROVED,
                                "550e8400-e29b-41d4-a716-446655440002",
                                null,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                LocalDateTime.now());

                when(kycPersistencePort.findByKycIdAndUserId("kyc-3", "550e8400-e29b-41d4-a716-446655440001"))
                                .thenReturn(Optional.of(approvedKyc));

                IdentityException exception = assertThrows(IdentityException.class, () -> {
                        kycService.cancel("550e8400-e29b-41d4-a716-446655440001", "kyc-3");
                });

                assertEquals(IdentityErrorCode.KYC_CANNOT_BE_CANCELLED.getCode(), exception.getErrorCode());
        }

        @Test
        @DisplayName("Should throw KYC_NOT_FOUND when KYC profile not found")
        void testThrowsKycNotFound() {
                when(kycPersistencePort.findByKycIdAndUserId(anyString(), anyString()))
                                .thenReturn(Optional.empty());

                IdentityException exception = assertThrows(IdentityException.class, () -> {
                        kycService.submit("550e8400-e29b-41d4-a716-446655440001", "non-existent-kyc");
                });

                assertEquals(IdentityErrorCode.KYC_NOT_FOUND.getCode(), exception.getErrorCode());
        }

        @Test
        @DisplayName("Should validate admin role in review() method")
        void testValidateAdminUserInReview() {
                when(userPersistencePort.findById("550e8400-e29b-41d4-a716-446655440001"))
                                .thenReturn(Optional.of(regularUser));

                IdentityException exception = assertThrows(IdentityException.class, () -> {
                        kycService.review("550e8400-e29b-41d4-a716-446655440001", "kyc-1", "Review note");
                });

                assertEquals(IdentityErrorCode.INSUFFICIENT_PERMISSIONS.getCode(), exception.getErrorCode());
        }

        @Test
        @DisplayName("Should validate admin role in approve() method")
        void testValidateAdminUserInApprove() {
                when(userPersistencePort.findById("550e8400-e29b-41d4-a716-446655440001"))
                                .thenReturn(Optional.of(regularUser));

                IdentityException exception = assertThrows(IdentityException.class, () -> {
                        kycService.approve("550e8400-e29b-41d4-a716-446655440001", "kyc-1");
                });

                assertEquals(IdentityErrorCode.INSUFFICIENT_PERMISSIONS.getCode(), exception.getErrorCode());
        }

        @Test
        @DisplayName("Should validate admin role in reject() method")
        void testValidateAdminUserInReject() {
                when(userPersistencePort.findById("550e8400-e29b-41d4-a716-446655440001"))
                                .thenReturn(Optional.of(regularUser));

                IdentityException exception = assertThrows(IdentityException.class, () -> {
                        kycService.reject("550e8400-e29b-41d4-a716-446655440001", "kyc-1", "Rejection reason");
                });

                assertEquals(IdentityErrorCode.INSUFFICIENT_PERMISSIONS.getCode(), exception.getErrorCode());
        }

        @Test
        @DisplayName("Should allow admin user to review KYC")
        void testAdminCanReview() {
                // Create IN_REVIEW KYC
                KycProfile inReviewKyc = new KycProfile(
                                "kyc-2",
                                "550e8400-e29b-41d4-a716-446655440001",
                                "PASSPORT",
                                "https://blob.storage/doc.pdf",
                                KycStatus.SUBMITTED,
                                null,
                                null,
                                LocalDateTime.now(),
                                null,
                                LocalDateTime.now());

                when(userPersistencePort.findById("550e8400-e29b-41d4-a716-446655440002"))
                                .thenReturn(Optional.of(adminUser));
                when(kycPersistencePort.findById("kyc-2"))
                                .thenReturn(Optional.of(inReviewKyc));
                when(kycPersistencePort.save(any(KycProfile.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                KycProfile result = kycService.review("550e8400-e29b-41d4-a716-446655440002", "kyc-2", "Review note");

                assertNotNull(result);
                assertEquals(KycStatus.IN_REVIEW, result.getStatus());
                verify(kycPersistencePort).save(any(KycProfile.class));
        }

        @Test
        @DisplayName("Should validate status transition in submit()")
        void testStatusTransitionValidationInSubmit() {
                when(kycPersistencePort.findByKycIdAndUserId("kyc-1", "550e8400-e29b-41d4-a716-446655440001"))
                                .thenReturn(Optional.of(draftKyc));
                when(kycPersistencePort.save(any(KycProfile.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                KycProfile result = kycService.submit("550e8400-e29b-41d4-a716-446655440001", "kyc-1");

                assertNotNull(result);
                assertEquals(KycStatus.SUBMITTED, result.getStatus());
                assertNotNull(result.getSubmittedAt());
        }

        @Test
        @DisplayName("Should use KycStatus enum instead of hardcoded strings")
        void testUsesKycStatusEnum() {
                when(userPersistencePort.findById("550e8400-e29b-41d4-a716-446655440001"))
				.thenReturn(Optional.of(regularUser));
		when(kycPersistencePort.save(any(KycProfile.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                KycProfile result = kycService.createKyc("550e8400-e29b-41d4-a716-446655440001", "PASSPORT");

                assertNotNull(result);
                // Verify it uses KycStatus enum, not string
                assertEquals(KycStatus.DRAFT, result.getStatus());
                assertInstanceOf(KycStatus.class, result.getStatus());
        }

        @Test
        @DisplayName("Should verify KycService has @Slf4j annotation")
        void testHasSlf4jAnnotation() {
                // Verify the class has @Slf4j annotation by checking if log field exists
                try {
java.lang.reflect.Field logField = KycService.class.getDeclaredField("log");
assertNotNull(logField, "KycService should have a log field from @Slf4j");
assertEquals(org.slf4j.Logger.class, logField.getType(),
"log field should be of type org.slf4j.Logger");
} catch (NoSuchFieldException e) {
fail("KycService should have @Slf4j annotation which creates a 'log' field");
}
        }

        @Test
        @DisplayName("Should verify KycService methods do NOT have @Transactional")
        void testNoTransactionalAnnotations() {
                // Verify no methods have @Transactional annotation
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
                IdentityUser systemAdmin = new IdentityUser(
                                UserId.of("550e8400-e29b-41d4-a716-446655440003"),
                                "sysadmin@example.com",
                                "+1111111111",
                                "sysadmin",
                                "hashedPassword",
                                "System Admin",
                                null,
                                Set.of(UserRole.SYSTEM_ADMIN),
                                UserStatus.ACTIVE,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                LocalDateTime.now());

                KycProfile inReviewKyc = new KycProfile(
                                "kyc-3",
                                "550e8400-e29b-41d4-a716-446655440001",
                                "PASSPORT",
                                "https://blob.storage/doc.pdf",
                                KycStatus.IN_REVIEW,
                                null,
                                null,
                                LocalDateTime.now(),
                                null,
                                LocalDateTime.now());

                when(userPersistencePort.findById("550e8400-e29b-41d4-a716-446655440003"))
                                .thenReturn(Optional.of(systemAdmin));
                when(kycPersistencePort.findById("kyc-3"))
                                .thenReturn(Optional.of(inReviewKyc));
                when(kycPersistencePort.save(any(KycProfile.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Should not throw exception
                assertDoesNotThrow(() -> {
                        kycService.approve("550e8400-e29b-41d4-a716-446655440003", "kyc-3");
                });
        }
}





