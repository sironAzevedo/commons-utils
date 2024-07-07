package com.br.azevedo.conversor.date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Classe de desserializacao do JSON para parametros do tipo LocalDate
 * @author fabrica.intera 
 *
 */
@Slf4j
public class ISOLocalDateDeserializer extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final String dateAsString = jp.getText();
        
        LocalDate localDateParsed = null;
        if(StringUtils.isNotBlank(dateAsString)) {
            try {
            	if(dateAsString.contains("-")) {
            		localDateParsed = LocalDate.parse(dateAsString);
            	}else if(dateAsString.contains("/")) {
            		localDateParsed = LocalDate.parse(dateAsString, DateFormat.FORMATER_DDMMYYYY);
            	}
            } catch (IllegalArgumentException e) {
                log.info("Formato de data e hora invalida: " + dateAsString);
            }
        }
        
        return localDateParsed;
    }
}