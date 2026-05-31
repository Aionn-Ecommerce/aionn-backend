package com.aionn.identity.infrastructure.media;

import com.aionn.identity.application.dto.media.result.UploadSignatureResult;
import com.aionn.identity.application.port.out.media.MediaUploadSignatureProviderPort;
import com.aionn.identity.infrastructure.config.properties.CloudinaryProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "identity.media", name = "provider", havingValue = "cloudinary")
public class CloudinaryMediaUploadSignatureProvider implements MediaUploadSignatureProviderPort {

    private static final String SIGNATURE_ALGORITHM = "SHA-1";

    private final CloudinaryProperties cloudinaryProperties;

    @Override
    public UploadSignatureResult generateAvatarUploadSignature(String userId) {
        String folder = cloudinaryProperties.avatarFolder() + "/" + userId;
        long timestamp = Instant.now().getEpochSecond();

        Map<String, String> params = new TreeMap<>();
        params.put("folder", folder);
        params.put("timestamp", String.valueOf(timestamp));
        params.put("eager", "c_fill,w_256,h_256,g_face");

        String signature = sign(params);

        log.debug("Generated avatar upload signature for userId={}, folder={}", userId, folder);
        return new UploadSignatureResult(
                signature,
                String.valueOf(timestamp),
                cloudinaryProperties.apiKey(),
                cloudinaryProperties.cloudName(),
                cloudinaryProperties.uploadUrl("image"),
                folder);
    }

    @Override
    public UploadSignatureResult generateKycDocumentUploadSignature(String userId) {
        String folder = cloudinaryProperties.kycFolder() + "/" + userId;
        long timestamp = Instant.now().getEpochSecond();

        Map<String, String> params = new TreeMap<>();
        params.put("folder", folder);
        params.put("timestamp", String.valueOf(timestamp));

        String signature = sign(params);

        log.debug("Generated KYC upload signature for userId={}, folder={}", userId, folder);
        return new UploadSignatureResult(
                signature,
                String.valueOf(timestamp),
                cloudinaryProperties.apiKey(),
                cloudinaryProperties.cloudName(),
                cloudinaryProperties.uploadUrl("auto"),
                folder);
    }

    private String sign(Map<String, String> params) {
        String toSign = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        try {
            MessageDigest digest = MessageDigest.getInstance(SIGNATURE_ALGORITHM);
            byte[] hash = digest.digest((toSign + cloudinaryProperties.apiSecret()).getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to compute Cloudinary signature", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
