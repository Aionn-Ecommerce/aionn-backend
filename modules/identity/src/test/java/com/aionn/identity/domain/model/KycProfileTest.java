package com.aionn.identity.domain.model;

import com.aionn.identity.domain.valueobject.KycReviewAnswer;
import com.aionn.identity.domain.valueobject.KycStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KycProfileTest {

    @Test
    void attachExternalProviderMarksProfileAsExternallyManaged() {
        KycProfile profile = new KycProfile(
                "kyc-1",
                "user-1",
                "PASSPORT",
                null,
                KycStatus.DRAFT,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.now());

        profile.attachExternalProvider("sumsub", "applicant-1", "basic-kyc", "pending", "corr-1");

        assertTrue(profile.isManagedExternally());
        assertEquals(KycStatus.SUBMITTED, profile.getStatus());
    }

    @Test
    void syncExternalReviewApprovesProfile() {
        KycProfile profile = new KycProfile(
                "kyc-1",
                "user-1",
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

        profile.syncExternalReview("completed", "corr-2", KycReviewAnswer.GREEN, "approved", null);

        assertEquals(KycStatus.APPROVED, profile.getStatus());
        assertEquals("SUMSUB", profile.getDecisionAdminId());
        assertNull(profile.getRejectReason());
        assertTrue(profile.getApprovedAt() != null);
    }

    @Test
    void syncExternalReviewRejectsProfileWithFallbackReason() {
        KycProfile profile = new KycProfile(
                "kyc-1",
                "user-1",
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

        profile.syncExternalReview("completed", "corr-2", KycReviewAnswer.RED, " ", "selfie mismatch");

        assertEquals(KycStatus.REJECTED, profile.getStatus());
        assertEquals("SUMSUB", profile.getDecisionAdminId());
        assertEquals("selfie mismatch", profile.getRejectReason());
        assertNull(profile.getApprovedAt());
    }
}
