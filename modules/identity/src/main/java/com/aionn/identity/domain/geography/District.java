package com.aionn.identity.domain.geography;

public record District(
        String code,
        String name,
        String nameEn,
        String provinceCode,
        boolean active) {
}
