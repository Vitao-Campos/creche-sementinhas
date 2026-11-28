package br.com.crechesementinhas.util;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * Máscaras de digitação (CPF, telefone e CEP): o campo aceita só números e vai
 * colocando pontos, traços e parênteses enquanto a pessoa digita. O que é gravado
 * continua sendo tratado pelas classes Cpf e Telefone, que removem a máscara.
 */
public final class Mascara {

    /** Classe só com métodos estáticos: não é instanciada. */
    private Mascara() {
    }

    /** Aplica a máscara de CPF (000.000.000-00) ao campo. */
    public static void cpf(TextField campo) {
        aplicar(campo, 11, Mascara::formatarCpf);
    }

    /** Aplica a máscara de telefone ((00) 00000-0000) ao campo. */
    public static void telefone(TextField campo) {
        aplicar(campo, 11, Mascara::formatarTelefone);
    }

    /**
     * Data dd/mm/aaaa a partir dos números digitados. Para o campo de texto de um DatePicker
     * (use datePicker.getEditor()): o DatePicker já tem formatador próprio, por isso aqui se
     * acompanha o texto em vez de trocar o formatador.
     */
    public static void data(TextField campo) {
        campo.textProperty().addListener((obs, antigo, novo) -> {
            String digitos = novo == null ? "" : novo.replaceAll("\\D", "");
            if (digitos.length() > 8) {
                digitos = digitos.substring(0, 8);
            }
            String formatado = formatarData(digitos);
            if (novo != null && !formatado.equals(novo)) {
                campo.setText(formatado);
            }
        });
    }

    /** Aplica a máscara de CEP (00000-000) ao campo. */
    public static void cep(TextField campo) {
        aplicar(campo, 8, Mascara::formatarCep);
    }

    // ------------------------------------------------------------------ formatos (parciais)

    /** Coloca pontos e traço nos números do CPF, conforme vão sendo digitados. */
    static String formatarCpf(String d) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < d.length(); i++) {
            if (i == 3 || i == 6) {
                s.append('.');
            } else if (i == 9) {
                s.append('-');
            }
            s.append(d.charAt(i));
        }
        return s.toString();
    }

    /** Coloca as barras nos números da data, conforme vão sendo digitados. */
    static String formatarData(String d) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < d.length(); i++) {
            if (i == 2 || i == 4) {
                s.append('/');
            }
            s.append(d.charAt(i));
        }
        return s.toString();
    }

    /** Coloca parênteses, espaço e traço nos números do telefone, conforme vão sendo digitados. */
    static String formatarTelefone(String d) {
        if (d.isEmpty()) {
            return "";
        }
        if (d.length() <= 2) {
            return "(" + d;
        }
        String ddd = d.substring(0, 2);
        String resto = d.substring(2);
        // 10 dígitos = fixo (4+4); 11 = celular (5+4)
        int corte = d.length() > 10 ? 5 : 4;
        if (resto.length() <= corte) {
            return "(" + ddd + ") " + resto;
        }
        return "(" + ddd + ") " + resto.substring(0, corte) + "-" + resto.substring(corte);
    }

    /** Coloca o traço nos números do CEP, conforme vão sendo digitados. */
    static String formatarCep(String d) {
        return d.length() <= 5 ? d : d.substring(0, 5) + "-" + d.substring(5);
    }

    // ------------------------------------------------------------------ mecânica

    /** Instala no campo um filtro que aceita só números, limita o tamanho e aplica o formato. */
    private static void aplicar(TextField campo, int maxDigitos, java.util.function.UnaryOperator<String> formato) {
        campo.setTextFormatter(new TextFormatter<String>(mudanca -> {
            if (!mudanca.isContentChange()) {
                return mudanca;
            }
            String digitos = mudanca.getControlNewText().replaceAll("\\D", "");
            if (digitos.length() > maxDigitos) {
                digitos = digitos.substring(0, maxDigitos);
            }
            String formatado = formato.apply(digitos);
            mudanca.setRange(0, mudanca.getControlText().length());
            mudanca.setText(formatado);
            mudanca.selectRange(formatado.length(), formatado.length());
            return mudanca;
        }));
    }
}
