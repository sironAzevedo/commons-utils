package com.br.azevedo.utils;

import com.br.azevedo.exception.EmptyResultDataAccessException;
import org.apache.commons.lang3.StringUtils;

public final class PhoneUtils {
    private final static int ZERO = 0;

    private PhoneUtils() {
    }

    public static Long obterDDICelular(String numeroTelefone) {
        validarCelular(numeroTelefone);
        return Long.valueOf(StringUtils.substring(numeroTelefone, 0, 2));
    }

    public static Long obterDDDCelular(String numeroTelefone) {
        validarCelular(numeroTelefone);
        return Long.valueOf(StringUtils.substring(numeroTelefone, 2, 4));
    }

    public static Long obterNumeroCelular(String numeroTelefone) {
        validarCelular(numeroTelefone);
        return Long.valueOf(StringUtils.substring(numeroTelefone, 4));
    }

    public static String validarDDI(int ddi) {
        return ddi != ZERO ? String.valueOf(ddi) : "00";
    }

    public static String validarDDD(int ddd) {
        return ddd != ZERO ? String.valueOf(ddd) : "00";
    }

    public static String validarNumeroCelular(Long numeroTelefone) {
        return numeroTelefone != ZERO ? String.valueOf(numeroTelefone) : "000000000";
    }

    private static void validarCelular(String numeroTelefone) {
        if(StringUtils.isEmpty(numeroTelefone)) {
            throw new EmptyResultDataAccessException("Numero celular invalido");
        }

        if(numeroTelefone.length() < 13) {
            throw new EmptyResultDataAccessException("Numero celular invalido");
        }
    }
}
