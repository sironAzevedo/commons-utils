package com.br.azevedo.conversor.serializer;

import com.br.azevedo.utils.MoedaUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalCurrencySerializer extends JsonSerializer<BigDecimal> {

    @Override
    public void serialize(BigDecimal value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        String formattedValue = MoedaUtils.BigDecimalToString(value);
        jsonGenerator.writeString(formattedValue);
    }
}
