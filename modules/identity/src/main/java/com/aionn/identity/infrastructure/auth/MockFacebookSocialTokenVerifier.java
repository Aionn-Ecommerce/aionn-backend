package com.aionn.identity.infrastructure.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "identity.auth.social.facebook", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockFacebookSocialTokenVerifier implements FacebookSocialTokenVerifier {

    private static final String PREFIX = "mock-facebook-";

    @Override
    public String verifyAndExtractUserId(String providerToken) {
        requireNotBlank(providerToken);
        log.warn("Using MockFacebookSocialTokenVerifier - DO NOT USE IN PRODUCTION");
        return PREFIX + digest(providerToken);
    }

    private String digest(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 12);
        } catch (NoSuchAlgorithmException ex) {
            return Integer.toHexString(value.hashCode());
        }
    }
}
