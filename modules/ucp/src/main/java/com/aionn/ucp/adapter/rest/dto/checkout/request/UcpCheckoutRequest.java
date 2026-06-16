package com.aionn.ucp.adapter.rest.dto.checkout.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UcpCheckoutRequest(
        @NotBlank String addressId,
        @NotBlank String paymentMethodId,
        String currency,
        BigDecimal shippingFee,
        @NotNull @Valid UcpCheckoutShippingAddressRequest shippingAddress) {
}
