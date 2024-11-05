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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BigDecimalCurrencyDeserializer extends JsonDeserializer<BigDecimal> {

    private static final String PADRAO_FORMATO_BR = "^\\d{1,3}(\\.\\d{3})*,\\d{2}$";
    NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    public BigDecimal deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String value = jsonParser.getText();
        verificarFormatoBrasileiro(value);
        return MoedaUtils.stringToBigDecimal(value);
    }

    public static void verificarFormatoBrasileiro(String valor) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile(PADRAO_FORMATO_BR);
        Matcher matcher = pattern.matcher(valor);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Valor fora do formato brasileiro (ex: 1.234,56 ou 127,27 )");
        }
    }
}
