package com.aionn.ucp.domain.model;

import java.util.List;
import java.util.Map;

public record UcpMetadata(
        String version,
        String status,
        Map<String, List<UcpCapability>> capabilities) {
}
