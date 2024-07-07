package com.br.azevedo.conversor.date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDate;

/**
 * 
 * Classe de Sserializacao do JSON para parametros do tipo Date
 * @author fabrica.intera.22011095
 *
 */
public class LocalDateSerializer extends JsonSerializer<LocalDate> {
	
    @Override
    public void serialize(LocalDate value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        if(value != null) {
            jgen.writeString(value.format(DateFormat.FORMATER_DDMMYYYY));
        }
    }
}
