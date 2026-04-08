package com.ecommerce.identity.adapter.rest.dto.media;

public record UploadSignatureResponse(
        String signature,
        String timestamp,
        String apiKey,
        String cloudName,
        String uploadUrl,
        String folder) {
}


