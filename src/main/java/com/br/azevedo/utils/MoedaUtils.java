package com.br.azevedo.utils;

import com.br.azevedo.exception.ApplicationException;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Objects;

public final class MoedaUtils {
    private MoedaUtils() {
        super();
    }

    public static BigDecimal stringToBigDecimal(String valor) {
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
        try {
            Number number = format.parse(valor);
            return BigDecimal.valueOf(number.doubleValue());
        } catch (ParseException e) {
            throw new ApplicationException("Error deserializing BigDecimal", e);
        }
    }

    public static String BigDecimalToString(BigDecimal valor) {
        if (Objects.isNull(valor)) {
            return null;
        }
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return format.format(valor);
    }
}
