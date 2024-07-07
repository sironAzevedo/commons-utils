package com.br.azevedo.conversor.date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Classe de Serializacao do JSON para parametros do tipo LocalDateTime utilizados nas telas.
 * Para a Classe de Serializacao padrão do JSON, utilizar o {@link ISOLocalDateTimeSerializer}
 */
public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    @Override
    public void serialize(LocalDateTime value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        jgen.writeString(value.format(DateFormat.FORMATER_DDMMYYYYHHMMSS));
    }
}