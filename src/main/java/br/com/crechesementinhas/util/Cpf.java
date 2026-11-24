package br.com.crechesementinhas.util;

/**
 * Funções de apoio para CPF: tirar a máscara, conferir os dígitos
 * verificadores e formatar para exibição (o CPF é único no banco, seção 6.2).
 */
public final class Cpf {

    private Cpf() {
    }

    /**
     * Deixa só os dígitos do CPF (tira pontos e traço).
     *
     * @param texto o CPF como foi digitado
     * @return só os dígitos, ou null se o texto estiver vazio
     */
    public static String normalizar(String texto) {
        if (texto == null) {
            return null;
        }
        String digitos = texto.replaceAll("\\D", "");
        return digitos.isEmpty() ? null : digitos;
    }

    /**
     * Confere se o CPF tem 11 dígitos e se os dois dígitos verificadores batem.
     *
     * @param texto o CPF, com ou sem máscara
     * @return true se o CPF for válido
     */
    public static boolean valido(String texto) {
        String cpf = normalizar(texto);
        if (cpf == null || cpf.length() != 11) {
            return false;
        }
        // 111.111.111-11 e parecidos passam na conta, mas não existem.
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }
        return calcularDigito(cpf, 9) == cpf.charAt(9) - '0'
                && calcularDigito(cpf, 10) == cpf.charAt(10) - '0';
    }

    /**
     * Formata 11 dígitos como 000.000.000-00.
     *
     * @param cpf os 11 dígitos
     * @return o CPF formatado; se não tiver 11 dígitos, devolve o texto como veio
     */
    public static String formatar(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "."
                + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    private static int calcularDigito(String cpf, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (cpf.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
