package com.br.azevedo.conversor.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class BigDecimalCurrencySerializer extends JsonSerializer<BigDecimal> {

    @Override
    public void serialize(BigDecimal value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        String formattedValue = format.format(value);
        jsonGenerator.writeString(formattedValue);
    }
}
