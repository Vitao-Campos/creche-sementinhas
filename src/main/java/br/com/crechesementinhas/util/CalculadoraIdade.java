package br.com.crechesementinhas.util;

import java.time.LocalDate;
import java.time.Period;

/**
 * Calcula a idade a partir da data de nascimento (RF-03).
 * A idade nunca é gravada no banco: é sempre calculada na hora.
 * Em meses, porque a faixa etária das turmas é em meses (RN-01).
 */
public final class CalculadoraIdade {

    private CalculadoraIdade() {
    }

    /**
     * Idade completa em meses.
     *
     * @param nascimento data de nascimento
     * @param referencia data de referência (hoje, ou a data da matrícula)
     * @return meses completos; 0 se faltar alguma data ou se o nascimento for depois da referência
     */
    public static long emMeses(LocalDate nascimento, LocalDate referencia) {
        if (nascimento == null || referencia == null || nascimento.isAfter(referencia)) {
            return 0;
        }
        return Period.between(nascimento, referencia).toTotalMonths();
    }

    /**
     * Idade escrita para a tela, como "1 ano e 3 meses" ou "8 meses".
     *
     * @param nascimento data de nascimento
     * @param referencia data de referência
     * @return o texto da idade
     */
    public static String descrever(LocalDate nascimento, LocalDate referencia) {
        long meses = emMeses(nascimento, referencia);
        if (meses < 1) {
            return "menos de 1 mês";
        }
        long anos = meses / 12;
        long resto = meses % 12;

        String textoAnos = anos == 0 ? "" : anos + (anos == 1 ? " ano" : " anos");
        String textoMeses = resto == 0 ? "" : resto + (resto == 1 ? " mês" : " meses");

        if (!textoAnos.isEmpty() && !textoMeses.isEmpty()) {
            return textoAnos + " e " + textoMeses;
        }
        return textoAnos + textoMeses;
    }
}
