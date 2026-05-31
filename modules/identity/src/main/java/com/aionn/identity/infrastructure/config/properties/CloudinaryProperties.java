package com.aionn.identity.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "identity.media.cloudinary")
public record CloudinaryProperties(
        String cloudName,
        String apiKey,
        String apiSecret,
        @DefaultValue("https://api.cloudinary.com/v1_1") String uploadBaseUrl,
        @DefaultValue("identity/avatars") String avatarFolder,
        @DefaultValue("identity/kyc") String kycFolder) {

    public String uploadUrl(String resourceType) {
        return uploadBaseUrl + "/" + cloudName + "/" + resourceType + "/upload";
    }
}
