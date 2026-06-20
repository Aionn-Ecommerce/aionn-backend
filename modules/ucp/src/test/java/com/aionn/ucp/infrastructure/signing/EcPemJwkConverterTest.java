package com.aionn.ucp.infrastructure.signing;

import com.aionn.ucp.domain.model.UcpJwkPublicKey;
import com.aionn.ucp.infrastructure.config.properties.UcpSigningProperties;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class EcPemJwkConverterTest {

    @Test
    void emptyPemReturnsEmpty() {
        UcpSigningProperties props = new UcpSigningProperties("k1", "ES256", "P-256", "", "");
        EcPemJwkConverter converter = new EcPemJwkConverter(props);

        assertThat(converter.toJwk()).isEmpty();
    }

    @Test
    void blankPemReturnsEmpty() {
        UcpSigningProperties props = new UcpSigningProperties("k1", "ES256", "P-256", "", "  \n  ");
        EcPemJwkConverter converter = new EcPemJwkConverter(props);

        assertThat(converter.toJwk()).isEmpty();
    }

    @Test
    void invalidPemReturnsEmptyAndDoesNotThrow() {
        UcpSigningProperties props = new UcpSigningProperties("k1", "ES256", "P-256", "",
                "-----BEGIN PUBLIC KEY-----\nnot-base64\n-----END PUBLIC KEY-----");
        EcPemJwkConverter converter = new EcPemJwkConverter(props);

        assertThat(converter.toJwk()).isEmpty();
    }

    @Test
    void validP256PemProducesEcJwkWithThirtyTwoByteCoords() throws Exception {
        KeyPair kp = generateP256();
        String pem = toPem(kp.getPublic().getEncoded());
        UcpSigningProperties props = new UcpSigningProperties(
                "key-id-xyz", "ES256", "P-256", "", pem);

        EcPemJwkConverter converter = new EcPemJwkConverter(props);
        Optional<UcpJwkPublicKey> jwk = converter.toJwk();

        assertThat(jwk).isPresent();
        UcpJwkPublicKey k = jwk.get();
        assertThat(k.kid()).isEqualTo("key-id-xyz");
        assertThat(k.kty()).isEqualTo("EC");
        assertThat(k.crv()).isEqualTo("P-256");
        assertThat(k.alg()).isEqualTo("ES256");
        assertThat(k.use()).isEqualTo("sig");

        // Base64url no-pad of 32 bytes is exactly 43 chars.
        assertThat(k.x()).hasSize(43);
        assertThat(k.y()).hasSize(43);

        // Decoded length must be 32 bytes (P-256 coordinate length).
        Base64.Decoder dec = Base64.getUrlDecoder();
        assertThat(dec.decode(k.x())).hasSize(32);
        assertThat(dec.decode(k.y())).hasSize(32);
    }

    @Test
    void converterPreservesKeyIdAndAlgorithmFromProperties() throws Exception {
        KeyPair kp = generateP256();
        String pem = toPem(kp.getPublic().getEncoded());
        UcpSigningProperties props = new UcpSigningProperties(
                "another-id", "ES256", "P-256", "", pem);

        UcpJwkPublicKey jwk = new EcPemJwkConverter(props).toJwk().orElseThrow();

        assertThat(jwk.kid()).isEqualTo("another-id");
        assertThat(jwk.alg()).isEqualTo("ES256");
    }

    private static KeyPair generateP256() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        return kpg.generateKeyPair();
    }

    private static String toPem(byte[] der) {
        String base64 = Base64.getEncoder().encodeToString(der);
        StringBuilder sb = new StringBuilder("-----BEGIN PUBLIC KEY-----\n");
        for (int i = 0; i < base64.length(); i += 64) {
            sb.append(base64, i, Math.min(i + 64, base64.length())).append('\n');
        }
        sb.append("-----END PUBLIC KEY-----");
        return sb.toString();
    }
}
