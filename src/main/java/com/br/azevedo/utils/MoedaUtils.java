package com.br.azevedo.utils;

import com.br.azevedo.exception.ApplicationException;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MoedaUtils {
    private static final String PADRAO_FORMATO_BR = "^\\d{1,3}(\\.\\d{3})*,\\d{2}$";


    private MoedaUtils() {
        super();
    }

    public static BigDecimal stringToBigDecimal(String valor) {
        NumberFormat format = NumberFormat.getInstance(new Locale("pt", "BR"));
        try {
            Number number = format.parse(valor);
            return BigDecimal.valueOf(number.doubleValue());
        } catch (ParseException e) {
            throw new ApplicationException("Error deserializing BigDecimal", e);
        }
    }

    public static String bigDecimalToString(BigDecimal valor) {
        if (Objects.isNull(valor)) {
            return null;
        }

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        // Remover o símbolo de moeda (R$)
        DecimalFormat decimalFormat = (DecimalFormat) format;
        DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
        symbols.setCurrencySymbol(""); // Define o símbolo de moeda como vazio
        decimalFormat.setDecimalFormatSymbols(symbols);

        return decimalFormat.format(valor).replace("\u00A0", "").trim(); // Remover espaços em branco
    }

    public static void verificarFormatoBrasileiro(String valor) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile(PADRAO_FORMATO_BR);
        Matcher matcher = pattern.matcher(valor);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Valor fora do formato brasileiro (ex: 1.234,56, 127,27 ou 0,00)");
        }
    }
}
