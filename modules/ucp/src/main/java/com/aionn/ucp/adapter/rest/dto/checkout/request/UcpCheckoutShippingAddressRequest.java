package com.aionn.ucp.adapter.rest.dto.checkout.request;

import jakarta.validation.constraints.NotBlank;

public record UcpCheckoutShippingAddressRequest(
        @NotBlank String addressId,
        @NotBlank String fullName,
        @NotBlank String phone,
        @NotBlank String addressLine,
        String wardCode,
        String districtCode,
        String provinceCode,
        @NotBlank String countryCode) {
}
