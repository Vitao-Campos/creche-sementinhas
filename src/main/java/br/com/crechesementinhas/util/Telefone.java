package br.com.crechesementinhas.util;

/**
 * Funções de apoio para telefone brasileiro: tirar a máscara, conferir o tamanho
 * e formatar para exibição.
 */
public final class Telefone {

    private Telefone() {
    }

    /** Deixa só os dígitos; null se o texto estiver vazio. */
    public static String normalizar(String texto) {
        if (texto == null) {
            return null;
        }
        String digitos = texto.replaceAll("\\D", "");
        return digitos.isEmpty() ? null : digitos;
    }

    /** Aceita DDD + 8 dígitos (fixo) ou DDD + 9 dígitos (celular). */
    public static boolean valido(String texto) {
        String digitos = normalizar(texto);
        return digitos != null && (digitos.length() == 10 || digitos.length() == 11);
    }

    /** (51) 91234-5678 ou (51) 1234-5678; outro tamanho volta como veio. */
    public static String formatar(String digitos) {
        if (digitos == null) {
            return "";
        }
        if (digitos.length() == 11) {
            return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 7) + "-" + digitos.substring(7);
        }
        if (digitos.length() == 10) {
            return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 6) + "-" + digitos.substring(6);
        }
        return digitos;
    }
}
