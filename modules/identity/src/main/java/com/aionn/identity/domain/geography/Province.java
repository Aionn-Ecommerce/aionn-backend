package com.aionn.identity.domain.geography;

public record Province(
        String code,
        String name,
        String nameEn,
        String countryCode,
        boolean active) {
}
