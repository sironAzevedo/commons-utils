package com.br.azevedo.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.io.Resources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class JsonUtils {

    private JsonUtils() {}

    public static <T> T convertJsonStringToObject(String pathToFile, Class<T> objectClass) {
        String json = getJson(pathToFile);
        return jsonToObject(json, objectClass);
    }

    private static String getJson(String pathToFile) {
        try {
            return Resources.toString(Resources.getResource(pathToFile), StandardCharsets.UTF_8);
        } catch (IOException var2) {
            throw new IllegalArgumentException(var2);
        }
    }

    public static <T> T  jsonToObject(final String value, final Class<T> clazz) {

        try {
            return objectMapper().readValue(value, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static <T> String  objetcToJson(final T object) {
        try {
            return objectMapper().writeValueAsString(object);
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(OffsetDateTime.class, new JsonSerializer<>() {
            @Override
            public void serialize(OffsetDateTime offsetDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
                jsonGenerator.writeString(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(offsetDateTime));
            }
        });

//        simpleModule.addDeserializer(LocalDateTime.class, new ISOLocalDateTimeDeserializer());
//        simpleModule.addDeserializer(LocalDate.class, new ISOLocalDateDeserializer());
        objectMapper.registerModule(simpleModule);
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
}
