package com.br.azevedo.conversor.deserializer;

import com.br.azevedo.utils.MoedaUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Component
public class BigDecimalCurrencyDeserializer extends JsonDeserializer<BigDecimal> {


    NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    public BigDecimal deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String value = jsonParser.getText();
        MoedaUtils.verificarFormatoBrasileiro(value);
        return MoedaUtils.stringToBigDecimal(value);
    }


}
