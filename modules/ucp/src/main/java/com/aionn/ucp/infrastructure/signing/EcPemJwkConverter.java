package com.aionn.ucp.infrastructure.signing;

import com.aionn.ucp.domain.model.UcpJwkPublicKey;
import com.aionn.ucp.infrastructure.config.properties.UcpSigningProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EcPemJwkConverter {

    private static final String PEM_BEGIN = "-----BEGIN PUBLIC KEY-----";
    private static final String PEM_END = "-----END PUBLIC KEY-----";

    private final UcpSigningProperties signingProperties;

    public Optional<UcpJwkPublicKey> toJwk() {
        String pem = signingProperties.publicKeyPem();
        if (pem == null || pem.isBlank()) {
            return Optional.empty();
        }
        try {
            String body = pem.replace(PEM_BEGIN, "")
                    .replace(PEM_END, "")
                    .replaceAll("\\s+", "");
            byte[] derBytes = Base64.getDecoder().decode(body);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(derBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            if (!(publicKey instanceof ECPublicKey ecPublicKey)) {
                throw new IllegalStateException("Public key is not EC");
            }
            byte[] xBytes = toFixedLength(ecPublicKey.getW().getAffineX().toByteArray(), 32);
            byte[] yBytes = toFixedLength(ecPublicKey.getW().getAffineY().toByteArray(), 32);
            String x = Base64.getUrlEncoder().withoutPadding().encodeToString(xBytes);
            String y = Base64.getUrlEncoder().withoutPadding().encodeToString(yBytes);
            return Optional.of(new UcpJwkPublicKey(
                    signingProperties.keyId(),
                    "EC",
                    signingProperties.curve(),
                    x,
                    y,
                    "sig",
                    signingProperties.algorithm()));
        } catch (Exception ex) {
            log.warn("Failed to parse UCP signing public key PEM: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private static byte[] toFixedLength(byte[] src, int len) {
        if (src.length == len) {
            return src;
        }
        if (src.length == len + 1 && src[0] == 0) {
            byte[] out = new byte[len];
            System.arraycopy(src, 1, out, 0, len);
            return out;
        }
        if (src.length < len) {
            byte[] out = new byte[len];
            System.arraycopy(src, 0, out, len - src.length, src.length);
            return out;
        }
        throw new IllegalStateException("EC coordinate longer than expected: " + src.length);
    }
}
