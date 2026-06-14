package com.aionn.ucp.application.dto.envelope;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Price(long amount, String currency) {
}
