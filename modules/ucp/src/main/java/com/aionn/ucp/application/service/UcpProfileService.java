package com.aionn.ucp.application.service;

import com.aionn.ucp.domain.model.UcpJwkPublicKey;
import com.aionn.ucp.domain.model.UcpMetadata;
import com.aionn.ucp.domain.model.UcpProfile;
import com.aionn.ucp.infrastructure.signing.EcPemJwkConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UcpProfileService {

    private final UcpEnvelopeService envelopeService;
    private final EcPemJwkConverter jwkConverter;

    public UcpProfile getProfile() {
        UcpMetadata ucp = envelopeService.buildSuccessMetadata();
        return new UcpProfile(ucp, buildSigningKeys());
    }

    private List<UcpJwkPublicKey> buildSigningKeys() {
        Optional<UcpJwkPublicKey> jwk = jwkConverter.toJwk();
        return jwk.map(List::of).orElseGet(List::of);
    }
}
