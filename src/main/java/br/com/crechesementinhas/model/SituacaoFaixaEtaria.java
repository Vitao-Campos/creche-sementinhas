package br.com.crechesementinhas.model;

import java.time.LocalDate;

/**
 * Situação da idade da criança em relação à faixa etária da turma (alerta RF-07).
 * A andarilha (RF-11 / RN-08) é a exceção: dentro do prazo não alerta;
 * com o prazo vencido volta a alertar e conta como fora da faixa.
 */
public enum SituacaoFaixaEtaria {

    DENTRO("Dentro da faixa", false),
    ULTIMO_MES("Último mês na faixa", true),
    ACIMA("Acima da faixa", true),
    ABAIXO("Abaixo da faixa", true),
    ANDARILHA("Andarilha", false),
    ANDARILHA_VENCIDA("Prazo vencido", true),
    SEM_MATRICULA("Sem matrícula ativa", false);

    private final String descricao;
    private final boolean alerta;

    SituacaoFaixaEtaria(String descricao, boolean alerta) {
        this.descricao = descricao;
        this.alerta = alerta;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Merece destaque na tela? */
    public boolean isAlerta() {
        return alerta;
    }

    /** Fora da faixa de verdade (entra no contador do alerta)? */
    public boolean isForaDaFaixa() {
        return this == ACIMA || this == ABAIXO || this == ANDARILHA_VENCIDA;
    }

    /**
     * Classifica a idade (em meses) em relação à faixa da turma (em meses).
     * ULTIMO_MES: a criança está no último mês da faixa e vai ultrapassá-la no mês que vem.
     */
    public static SituacaoFaixaEtaria de(long idadeMeses, int minimaMeses, int maximaMeses) {
        if (idadeMeses < minimaMeses) {
            return ABAIXO;
        }
        if (idadeMeses > maximaMeses) {
            return ACIMA;
        }
        return idadeMeses == maximaMeses ? ULTIMO_MES : DENTRO;
    }

    /**
     * RN-08: a matrícula andarilha é a exceção da faixa etária.
     * No prazo (até o dia andarilhaAte, inclusive) não alerta; depois, ou sem prazo, alerta.
     */
    public static SituacaoFaixaEtaria deAndarilha(LocalDate andarilhaAte, LocalDate hoje) {
        return andarilhaAte != null && !hoje.isAfter(andarilhaAte) ? ANDARILHA : ANDARILHA_VENCIDA;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
