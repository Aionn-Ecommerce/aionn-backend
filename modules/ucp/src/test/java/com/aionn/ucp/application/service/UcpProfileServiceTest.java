package com.aionn.ucp.application.service;

import com.aionn.ucp.domain.model.UcpJwkPublicKey;
import com.aionn.ucp.domain.model.UcpMetadata;
import com.aionn.ucp.domain.model.UcpProfile;
import com.aionn.ucp.infrastructure.signing.EcPemJwkConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UcpProfileServiceTest {

    @Mock
    private UcpEnvelopeService envelopeService;
    @Mock
    private EcPemJwkConverter jwkConverter;

    private UcpProfileService service;

    @BeforeEach
    void setUp() {
        service = new UcpProfileService(envelopeService, jwkConverter);
    }

    @Test
    void getProfileReturnsEmptyKeysWhenConverterHasNothing() {
        UcpMetadata md = new UcpMetadata("2025-01-01", "success", Map.of());
        when(envelopeService.buildSuccessMetadata()).thenReturn(md);
        when(jwkConverter.toJwk()).thenReturn(Optional.empty());

        UcpProfile profile = service.getProfile();

        assertThat(profile.ucp()).isSameAs(md);
        assertThat(profile.signingKeys()).isEmpty();
    }

    @Test
    void getProfilePopulatesSigningKeysWhenConverterReturnsJwk() {
        UcpMetadata md = new UcpMetadata("2025-01-01", "success", Map.of());
        UcpJwkPublicKey jwk = new UcpJwkPublicKey("k1", "EC", "P-256", "x", "y", "sig", "ES256");
        when(envelopeService.buildSuccessMetadata()).thenReturn(md);
        when(jwkConverter.toJwk()).thenReturn(Optional.of(jwk));

        UcpProfile profile = service.getProfile();

        assertThat(profile.signingKeys()).containsExactly(jwk);
    }
}
