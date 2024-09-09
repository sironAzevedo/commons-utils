package com.br.azevedo.conversor.serializer;

import com.br.azevedo.utils.MoedaUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalCurrencySerializer extends StdSerializer<BigDecimal> {

    public BigDecimalCurrencySerializer() {
        super(BigDecimal.class);
    }

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        String formattedValue = MoedaUtils.BigDecimalToString(value);
        gen.writeString(formattedValue);
    }
}
