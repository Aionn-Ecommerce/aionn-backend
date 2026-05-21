package com.aionn.identity.infrastructure.media;

import com.aionn.identity.application.dto.media.result.UploadSignatureResult;
import com.aionn.identity.application.port.out.media.MediaUploadSignatureProvider;
import com.aionn.identity.infrastructure.config.properties.CloudinaryProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Production Cloudinary upload-signature provider. Generates HMAC-SHA256 signed
 * parameters so the client can upload directly to Cloudinary without proxying
 * through our server.
 * <p>
 * Activate with {@code identity.media.provider=cloudinary}.
 * <p>
 * The signature covers: folder, timestamp, and any eager transformations.
 * Cloudinary verifies the signature server-side; only uploads matching
 * these parameters are accepted.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "identity.media", name = "provider", havingValue = "cloudinary")
public class CloudinaryMediaUploadSignatureProvider implements MediaUploadSignatureProvider {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final CloudinaryProperties cloudinaryProperties;

    @Override
    public UploadSignatureResult generateAvatarUploadSignature(String userId) {
        String folder = cloudinaryProperties.avatarFolder() + "/" + userId;
        long timestamp = Instant.now().getEpochSecond();

        Map<String, String> params = new TreeMap<>();
        params.put("folder", folder);
        params.put("timestamp", String.valueOf(timestamp));
        // Restrict to images only for avatars
        params.put("resource_type", "image");
        // Auto-crop and resize for avatar consistency
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
        // KYC documents can be images or PDFs
        params.put("resource_type", "auto");

        String signature = sign(params);

        log.debug("Generated KYC doc upload signature for userId={}, folder={}", userId, folder);
        return new UploadSignatureResult(
                signature,
                String.valueOf(timestamp),
                cloudinaryProperties.apiKey(),
                cloudinaryProperties.cloudName(),
                cloudinaryProperties.uploadUrl("auto"),
                folder);
    }

    /**
     * Generate Cloudinary API signature. The signature is computed as:
     * HMAC-SHA256(param1=value1&param2=value2...&apiSecret)
     * where parameters are sorted alphabetically.
     */
    private String sign(Map<String, String> params) {
        String toSign = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(
                    cloudinaryProperties.apiSecret().getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM);
            mac.init(secretKey);
            byte[] hash = mac.doFinal(toSign.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
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
