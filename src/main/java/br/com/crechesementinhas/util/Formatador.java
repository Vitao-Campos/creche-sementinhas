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
    
    /** "35678912300" -> "356.789.123-00" */
    public static String cpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf == null ? "" : cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "."
                + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }
    
    /**
     * Lê o que a secretária digitou como dinheiro.
     * Aceita "1150", "1150,00", "1.150,00", "R$ 1.150,00" e "1150.00".
     */
    public static BigDecimal lerDinheiro(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Informe o valor da mensalidade.");
        }
        String limpo = texto.replace("R$", "").replace(" ", "").trim();
        if (limpo.contains(",")) {
            limpo = limpo.replace(".", "").replace(",", ".");
        }
        try {
            return new BigDecimal(limpo);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor da mensalidade inválido: " + texto);
        }
    }
}