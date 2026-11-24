package br.com.crechesementinhas.service;

import br.com.crechesementinhas.model.FaixaMensalidade;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Cálculos de mensalidade da Frente B, sem acesso a banco (para poder testar sozinhos).
 */
public final class CalculadoraMensalidade {

    /** RN-12: o desligamento exige comunicação com 30 dias corridos de antecedência. */
    public static final int AVISO_PREVIO_DIAS = 30;

    private CalculadoraMensalidade() {
    }

    /**
     * RN-07: idade em anos usada para a mensalidade.
     * O reajuste acontece NO MÊS do aniversário (cláusula do contrato): no mês em que
     * a criança faz aniversário, ela já é cobrada pela nova faixa, desde o dia 1º.
     *
     * @return anos completos contando o mês do aniversário inteiro; 0 se as datas forem inválidas
     */
    public static int idadeEmAnosParaMensalidade(LocalDate nascimento, LocalDate referencia) {
        if (nascimento == null || referencia == null || nascimento.isAfter(referencia)) {
            return 0;
        }
        int anos = referencia.getYear() - nascimento.getYear();
        if (referencia.getMonthValue() < nascimento.getMonthValue()) {
            anos--;
        }
        return Math.max(0, anos);
    }

    /** RN-07: encontra a faixa da tabela que contém a idade (em anos). */
    public static Optional<FaixaMensalidade> faixaPara(List<FaixaMensalidade> faixas, int idadeAnos) {
        return faixas.stream().filter(f -> f.contem(idadeAnos)).findFirst();
    }

    /** Dias cobrados no mês do desligamento: do dia 1º até a data de desligamento, inclusive. */
    public static int diasCobrados(LocalDate dataDesligamento) {
        return dataDesligamento.getDayOfMonth();
    }

    /**
     * RN-030 (modelo de domínio): valor proporcional do último mês = mensalidade × dias cobrados ÷ dias do mês,
     * arredondado para centavos (meio para cima).
     */
    public static BigDecimal proporcional(BigDecimal mensalidade, LocalDate dataDesligamento) {
        if (mensalidade == null || mensalidade.signum() <= 0) {
            throw new IllegalArgumentException("Mensalidade inválida para o cálculo proporcional.");
        }
        if (dataDesligamento == null) {
            throw new IllegalArgumentException("Informe a data de desligamento.");
        }
        int diasNoMes = YearMonth.from(dataDesligamento).lengthOfMonth();
        return mensalidade
                .multiply(BigDecimal.valueOf(diasCobrados(dataDesligamento)))
                .divide(BigDecimal.valueOf(diasNoMes), 2, RoundingMode.HALF_UP);
    }

    /** RN-12: primeira data de saída aceita quando o desligamento é registrado em "hoje". */
    public static LocalDate dataMinimaDesligamento(LocalDate hoje) {
        return hoje.plusDays(AVISO_PREVIO_DIAS);
    }
}
