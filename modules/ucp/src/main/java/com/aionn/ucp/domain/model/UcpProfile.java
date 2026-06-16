package com.aionn.ucp.domain.model;

import java.util.List;

public record UcpProfile(
        UcpMetadata ucp,
        List<UcpJwkPublicKey> signingKeys) {
}
