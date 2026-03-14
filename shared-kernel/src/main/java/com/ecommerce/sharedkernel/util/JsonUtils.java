package com.ecommerce.sharedkernel.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class JsonUtils {

    private JsonUtils() {
    }

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static String toJson(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonException("Failed to serialize object to JSON", e);
        }
    }

    public static String toPrettyJson(Object object) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonException("Failed to serialize object to pretty JSON", e);
        }
    }

    public static Optional<String> toJsonSafe(Object object) {
        try {
            return Optional.of(MAPPER.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new JsonException("Failed to deserialize JSON to " + type.getSimpleName(), e);
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return MAPPER.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new JsonException("Failed to deserialize JSON", e);
        }
    }

    public static <T> Optional<T> fromJsonSafe(String json, Class<T> type) {
        try {
            return Optional.of(MAPPER.readValue(json, type));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object object) {
        return MAPPER.convertValue(object, Map.class);
    }

    public static <T> List<T> toList(Object object, Class<T> elementType) {
        return MAPPER.convertValue(object,
                MAPPER.getTypeFactory().constructCollectionType(List.class, elementType));
    }

    public static <T> T convert(Object source, Class<T> targetType) {
        return MAPPER.convertValue(source, targetType);
    }

    public static JsonNode toTree(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new JsonException("Failed to parse JSON into tree", e);
        }
    }

    public static class JsonException extends RuntimeException {
        public JsonException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}