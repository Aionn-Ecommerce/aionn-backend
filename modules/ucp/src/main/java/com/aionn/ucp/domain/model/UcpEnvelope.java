package com.aionn.ucp.domain.model;

public record UcpEnvelope<T>(
        UcpMetadata ucp,
        T data) {

    public static <T> UcpEnvelope<T> success(UcpMetadata ucp, T data) {
        return new UcpEnvelope<>(ucp, data);
    }
}
