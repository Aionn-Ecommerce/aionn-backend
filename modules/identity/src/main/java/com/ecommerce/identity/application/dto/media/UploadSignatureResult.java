package com.ecommerce.identity.application.dto.media;

public record UploadSignatureResult(
        String signature,
        String timestamp,
        String apiKey,
        String cloudName,
        String uploadUrl,
        String folder) {
}
