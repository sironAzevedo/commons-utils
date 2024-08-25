package com.br.azevedo.utils;

import java.util.Arrays;
import java.util.List;

public final class ConstantesUtils {
    private ConstantesUtils() {
        super();
    }

    public static List<String> MESES = Arrays.asList(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    );

    public static String[] PACKAGES = {
            "br.com", "com.br", "com.br.azevedo"
    };

}
