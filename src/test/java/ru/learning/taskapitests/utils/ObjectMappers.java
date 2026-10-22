package ru.learning.taskapitests.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class ObjectMappers {
    private ObjectMappers() {}

    public static ObjectMapper create() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())                    // OffsetDateTime, LocalDate
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // даты строкой ISO-8601
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
}