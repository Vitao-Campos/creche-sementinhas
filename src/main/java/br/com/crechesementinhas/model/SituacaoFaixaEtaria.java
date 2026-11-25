package br.com.crechesementinhas.model;

/**
 * Situação da idade da criança em relação à faixa etária da turma (alerta da Frente E).
 */
public enum SituacaoFaixaEtaria {

    DENTRO("Dentro da faixa", false),
    ULTIMO_MES("Último mês na faixa", true),
    ACIMA("Acima da faixa", true),
    ABAIXO("Abaixo da faixa", true),
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

    /** Fora da faixa de verdade (acima ou abaixo)? */
    public boolean isForaDaFaixa() {
        return this == ACIMA || this == ABAIXO;
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

    @Override
    public String toString() {
        return descricao;
    }
}
