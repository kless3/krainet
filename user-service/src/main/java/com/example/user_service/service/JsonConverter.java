package com.example.user_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConverter {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    public static <T> T convertFromJson(String json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }
}