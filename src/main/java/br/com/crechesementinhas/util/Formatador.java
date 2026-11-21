package br.com.crechesementinhas.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Converte valores para o formato brasileiro (datas, dinheiro, CPF).
 */
public final class Formatador {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale BRASIL = Locale.of("pt", "BR");

    private Formatador() {
    }

    /** 2026-11-20 -> "20/11/2026" */
    public static String data(LocalDate data) {
        return data == null ? "" : data.format(DATA);
    }

    /** 1150.5 -> "R$ 1.150,50" */
    public static String dinheiro(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        // O Java usa um espaço especial depois do "R$"; trocamos por espaço normal.
        return NumberFormat.getCurrencyInstance(BRASIL).format(valor).replace('\u00A0', ' ');
    }
}