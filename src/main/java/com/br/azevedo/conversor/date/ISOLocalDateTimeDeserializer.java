package com.br.azevedo.conversor.date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe de Desserializacao do JSON para parametros do tipo LocalDateTime
 */
@Slf4j
public class ISOLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        String dateTimeAsString = jp.getText();

        if (StringUtils.isBlank(dateTimeAsString)) {
            return null;
        }

        dateTimeAsString = dateTimeAsString.trim();
        try {
            // Remove excesso de caracteres, se necessário
            if (dateTimeAsString.length() > 19) {
                dateTimeAsString = dateTimeAsString.substring(0, 19);
            }

            // Se contém somente a data, adicionar a hora inicial
            if (dateTimeAsString.length() == 10) {
                dateTimeAsString += " 00:00:00";
            }

            // Substituir 'T' por espaço, se presente
            if (dateTimeAsString.contains("T")) {
                dateTimeAsString = dateTimeAsString.replace("T", " ");
            }

            // Escolher o formatador apropriado
            DateTimeFormatter formatter = dateTimeAsString.contains("/")
                    ? DateFormat.FORMATER_DDMMYYYYHHMMSS
                    : DateFormat.FORMATER_YYYYMMDDHHMMSS;

            return LocalDateTime.parse(dateTimeAsString, formatter);
        } catch (IllegalArgumentException e) {
            log.info("Formato de data e hora invalida: {}", dateTimeAsString);
            return null;
        }
    }
}