package com.aionn.identity.adapter.rest.dto.security;

public record MfaSetupResponse(
        String secret,
        String otpauthUri,
        String issuer,
        String accountName) {
}
