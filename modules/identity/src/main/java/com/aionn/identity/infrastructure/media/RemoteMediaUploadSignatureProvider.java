package com.aionn.identity.infrastructure.media;

import com.aionn.identity.application.dto.media.result.UploadSignatureResult;
import com.aionn.identity.application.port.out.media.MediaUploadSignatureProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Real CDN/storage signer (stub). Hook into Cloudinary / S3 / GCS once the
 * provider is selected. Until then this fails closed so we never silently
 * return a fake-looking signature in production.
 */
@Component
@ConditionalOnProperty(prefix = "identity.media", name = "provider", havingValue = "remote")
public class RemoteMediaUploadSignatureProvider implements MediaUploadSignatureProvider {

    @Override
    public UploadSignatureResult generateAvatarUploadSignature(String userId) {
        throw new UnsupportedOperationException("Remote media signature provider is not implemented yet");
    }

    @Override
    public UploadSignatureResult generateKycDocumentUploadSignature(String userId) {
        throw new UnsupportedOperationException("Remote media signature provider is not implemented yet");
    }
}

