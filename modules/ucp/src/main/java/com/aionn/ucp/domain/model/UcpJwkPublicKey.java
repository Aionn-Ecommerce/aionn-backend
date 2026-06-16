package com.aionn.ucp.domain.model;

public record UcpJwkPublicKey(
        String kid,
        String kty,
        String crv,
        String x,
        String y,
        String use,
        String alg) {
}
