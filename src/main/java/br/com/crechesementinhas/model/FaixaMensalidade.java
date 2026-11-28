package br.com.crechesementinhas.model;

import java.math.BigDecimal;

/**
 * Uma linha da tabela de mensalidades por faixa etária (RN-07), tabela "faixa_mensalidade".
 * A faixa é pela idade em ANOS completos; idadeMaximaAnos null = sem limite superior.
 */
public class FaixaMensalidade {

    private Integer idFaixaMensalidade;
    private String descricao;
    private int idadeMinimaAnos;
    private Integer idadeMaximaAnos;
    private BigDecimal valor;

    /** Cria uma faixa vazia (usada ao ler do banco). */
    public FaixaMensalidade() {
    }

    /** Cria uma faixa de mensalidade com todos os dados. */
    public FaixaMensalidade(Integer id, String descricao, int idadeMinimaAnos,
                            Integer idadeMaximaAnos, BigDecimal valor) {
        this.idFaixaMensalidade = id;
        this.descricao = descricao;
        this.idadeMinimaAnos = idadeMinimaAnos;
        this.idadeMaximaAnos = idadeMaximaAnos;
        this.valor = valor;
    }

    /** Diz se a idade (em anos) pertence a esta faixa. */
    public boolean contem(int idadeAnos) {
        return idadeAnos >= idadeMinimaAnos
                && (idadeMaximaAnos == null || idadeAnos <= idadeMaximaAnos);
    }

    /** Devolve o identificador da faixa de mensalidade. */
    public Integer getIdFaixaMensalidade() { return idFaixaMensalidade; }
    /** Define o identificador da faixa de mensalidade. */
    public void setIdFaixaMensalidade(Integer id) { this.idFaixaMensalidade = id; }

    /** Devolve a descrição. */
    public String getDescricao() { return descricao; }
    /** Define a descrição. */
    public void setDescricao(String descricao) { this.descricao = descricao; }

    /** Devolve a idade mínima, em anos. */
    public int getIdadeMinimaAnos() { return idadeMinimaAnos; }
    /** Define a idade mínima, em anos. */
    public void setIdadeMinimaAnos(int idadeMinimaAnos) { this.idadeMinimaAnos = idadeMinimaAnos; }

    /** Devolve a idade máxima, em anos. */
    public Integer getIdadeMaximaAnos() { return idadeMaximaAnos; }
    /** Define a idade máxima, em anos. */
    public void setIdadeMaximaAnos(Integer idadeMaximaAnos) { this.idadeMaximaAnos = idadeMaximaAnos; }

    /** Devolve o valor. */
    public BigDecimal getValor() { return valor; }
    /** Define o valor. */
    public void setValor(BigDecimal valor) { this.valor = valor; }

    /** Texto da faixa nas listas: descrição e valor. */
    @Override
    public String toString() {
        return descricao;
    }
}
