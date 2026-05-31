package com.aionn.identity.domain.geography;

public record Ward(
        String code,
        String name,
        String nameEn,
        String districtCode,
        boolean active) {
}
