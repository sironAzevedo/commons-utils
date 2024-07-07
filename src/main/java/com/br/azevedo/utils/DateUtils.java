package com.br.azevedo.utils;

import com.br.azevedo.conversor.date.DateRange;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

public final class DateUtils {
    private DateUtils() {
        super();
    }

    public static LocalDateTime[] getMesAno(int mes, int ano) {
        LocalDateTime dataInicial = LocalDateTime.of(ano, mes, 1, 0, 0);
        LocalDateTime dataFinal = dataInicial.withDayOfMonth(dataInicial.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);

        return new LocalDateTime[]{dataInicial, dataFinal };
    }

    /**
     * Retorna um intervalo de datas representando o início e o fim do ano fornecido.
     *
     * @param ano o ano para o qual obter as datas inicial e final.
     * @return um objeto DateRange contendo as datas inicial e final.
     */
    public static DateRange getDateRangeForYear(int ano) {
        LocalDateTime dataInicial = LocalDateTime.of(ano, 1, 1, 0, 0);
        LocalDateTime dataFinal = LocalDateTime.of(ano, 12, 31, 23, 59, 59);
        int anoAtual = LocalDate.now().getYear();

        if (ano == anoAtual) {
            LocalDate ultimoDiaMesAtual = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
            dataFinal = LocalDateTime.of(ultimoDiaMesAtual, LocalDateTime.MAX.toLocalTime());
        }

        return new DateRange(dataInicial, dataFinal);
    }
}
