package com.aionn.ucp.domain.model;

public record UcpCapability(
        String name,
        String version,
        String spec,
        String schema) {
}
