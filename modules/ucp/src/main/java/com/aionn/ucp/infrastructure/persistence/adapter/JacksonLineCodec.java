package com.aionn.ucp.infrastructure.persistence.adapter;

import com.aionn.ucp.application.port.out.CheckoutSessionPersistencePort.LineCodec;
import com.aionn.ucp.application.port.out.CheckoutSessionPersistencePort.LineItemSnapshot;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JacksonLineCodec implements LineCodec {

    private final ObjectMapper objectMapper;

    @Override
    public String encode(List<LineItemSnapshot> lines) {
        try {
            return objectMapper.writeValueAsString(lines);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to encode line items", ex);
        }
    }

    @Override
    public List<LineItemSnapshot> decode(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<LineItemSnapshot>>() {
            });
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to decode line items", ex);
        }
    }
}
