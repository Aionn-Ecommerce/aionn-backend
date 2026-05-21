package com.aionn.identity.infrastructure.media;

import com.aionn.identity.application.dto.media.result.UploadSignatureResult;
import com.aionn.identity.application.port.out.media.MediaUploadSignatureProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Development-mode upload signature provider. Returns placeholder values that
 * mimic the shape of a real CDN response. Activate by leaving
 * {@code identity.media.provider} unset or {@code mock}.
 */
@Component
@ConditionalOnProperty(prefix = "identity.media", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockMediaUploadSignatureProvider implements MediaUploadSignatureProvider {

    @Override
    public UploadSignatureResult generateAvatarUploadSignature(String userId) {
        return placeholder("identity/avatars/" + userId);
    }

    @Override
    public UploadSignatureResult generateKycDocumentUploadSignature(String userId) {
        return placeholder("identity/kyc/" + userId);
    }

    private UploadSignatureResult placeholder(String folder) {
        return new UploadSignatureResult(
                "mock-signature",
                String.valueOf(Instant.now().getEpochSecond()),
                "mock-api-key",
                "mock-cloud",
                "https://example.invalid/mock-upload",
                folder);
    }
}

