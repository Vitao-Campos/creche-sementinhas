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

    public FaixaMensalidade() {
    }

    public FaixaMensalidade(Integer id, String descricao, int idadeMinimaAnos,
                            Integer idadeMaximaAnos, BigDecimal valor) {
        this.idFaixaMensalidade = id;
        this.descricao = descricao;
        this.idadeMinimaAnos = idadeMinimaAnos;
        this.idadeMaximaAnos = idadeMaximaAnos;
        this.valor = valor;
    }

    public boolean contem(int idadeAnos) {
        return idadeAnos >= idadeMinimaAnos
                && (idadeMaximaAnos == null || idadeAnos <= idadeMaximaAnos);
    }

    public Integer getIdFaixaMensalidade() { return idFaixaMensalidade; }
    public void setIdFaixaMensalidade(Integer id) { this.idFaixaMensalidade = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public int getIdadeMinimaAnos() { return idadeMinimaAnos; }
    public void setIdadeMinimaAnos(int idadeMinimaAnos) { this.idadeMinimaAnos = idadeMinimaAnos; }

    public Integer getIdadeMaximaAnos() { return idadeMaximaAnos; }
    public void setIdadeMaximaAnos(Integer idadeMaximaAnos) { this.idadeMaximaAnos = idadeMaximaAnos; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    @Override
    public String toString() {
        return descricao;
    }
}
