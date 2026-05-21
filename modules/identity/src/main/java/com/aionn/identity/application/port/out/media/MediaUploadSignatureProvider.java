package com.aionn.identity.application.port.out.media;

import com.aionn.identity.application.dto.media.result.UploadSignatureResult;

/**
 * Output port for direct-upload signature generation. Two implementations are
 * planned per project convention:
 *
 * <ul>
 * <li>{@code MockMediaUploadSignatureProvider} - assumes signing succeeds and
 * returns deterministic placeholders. Default for dev/test.</li>
 * <li>{@code RemoteMediaUploadSignatureProvider} - real CDN/storage signer.
 * Stub today; wire up when the storage provider is locked in.</li>
 * </ul>
 */
public interface MediaUploadSignatureProvider {

    UploadSignatureResult generateAvatarUploadSignature(String userId);

    UploadSignatureResult generateKycDocumentUploadSignature(String userId);
}

