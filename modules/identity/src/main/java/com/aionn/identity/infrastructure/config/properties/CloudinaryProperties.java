package com.aionn.identity.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Cloudinary configuration for direct-upload signature generation.
 * <p>
 * Required environment variables for production:
 * <ul>
 * <li>{@code CLOUDINARY_CLOUD_NAME}</li>
 * <li>{@code CLOUDINARY_API_KEY}</li>
 * <li>{@code CLOUDINARY_API_SECRET}</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "identity.media.cloudinary")
public record CloudinaryProperties(
        String cloudName,
        String apiKey,
        String apiSecret,
        @DefaultValue("https://api.cloudinary.com/v1_1") String uploadBaseUrl,
        @DefaultValue("identity/avatars") String avatarFolder,
        @DefaultValue("identity/kyc") String kycFolder,
        @DefaultValue("600") int signatureExpirySeconds) {

    /**
     * Build the upload URL for a given resource type.
     */
    public String uploadUrl(String resourceType) {
        return uploadBaseUrl + "/" + cloudName + "/" + resourceType + "/upload";
    }
}
