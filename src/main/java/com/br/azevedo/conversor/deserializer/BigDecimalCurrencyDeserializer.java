package com.br.azevedo.conversor.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

@Component
public class BigDecimalCurrencyDeserializer extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String value = jsonParser.getText();
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
        try {
            Number number = format.parse(value);
            return BigDecimal.valueOf(number.doubleValue());
        } catch (ParseException e) {
            throw new IOException("Error deserializing BigDecimal", e);
        }
    }
}
