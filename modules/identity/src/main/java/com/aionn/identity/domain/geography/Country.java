package com.aionn.identity.domain.geography;

public record Country(
        String code,
        String name,
        String nameEn,
        String phoneCode,
        boolean active) {
}
