package br.com.crechesementinhas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Uma VERSÃO do contrato de uma matrícula (tabela "contrato").
 * Cada renovação ou alteração gera um objeto novo, com versão + 1 (RF-08).
 */
public class Contrato {

    private int idContrato;
    private int idMatricula;
    private int idResponsavelFinanceiro;
    private String nomeResponsavelFinanceiro; // só para mostrar na tabela (vem do JOIN)
    private String plano;
    private String formaPagamento;            // BOLETO, PIX, CARTAO ou DINHEIRO
    private int diaVencimento;                // coluna data_vencimento_mensalidade
    private BigDecimal valorMensalidade;      // BigDecimal: dinheiro sem erro de arredondamento
    private LocalDate vigenciaAte;            // coluna vigencia_contrato
    private LocalDate dataAssinatura;         // vazia (null) até o responsável legal assinar
    private LocalDate dataSaida;
    private int versao;
    private String status;                    // VIGENTE, ENCERRADO ou CANCELADO

    // ---------- getters e setters ----------

    /** Devolve o identificador do contrato. */
    public int getIdContrato() { return idContrato; }
    /** Define o identificador do contrato. */
    public void setIdContrato(int idContrato) { this.idContrato = idContrato; }

    /** Devolve o identificador da matrícula. */
    public int getIdMatricula() { return idMatricula; }
    /** Define o identificador da matrícula. */
    public void setIdMatricula(int idMatricula) { this.idMatricula = idMatricula; }

    /** Devolve o identificador do responsável financeiro. */
    public int getIdResponsavelFinanceiro() { return idResponsavelFinanceiro; }
    /** Define o identificador do responsável financeiro. */
    public void setIdResponsavelFinanceiro(int id) { this.idResponsavelFinanceiro = id; }

    /** Devolve o nome do responsável financeiro. */
    public String getNomeResponsavelFinanceiro() { return nomeResponsavelFinanceiro; }
    /** Define o nome do responsável financeiro. */
    public void setNomeResponsavelFinanceiro(String nome) { this.nomeResponsavelFinanceiro = nome; }

    /** Devolve o plano contratado. */
    public String getPlano() { return plano; }
    /** Define o plano contratado. */
    public void setPlano(String plano) { this.plano = plano; }

    /** Devolve a forma de pagamento. */
    public String getFormaPagamento() { return formaPagamento; }
    /** Define a forma de pagamento. */
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    /** Devolve o dia de vencimento da mensalidade. */
    public int getDiaVencimento() { return diaVencimento; }
    /** Define o dia de vencimento da mensalidade. */
    public void setDiaVencimento(int diaVencimento) { this.diaVencimento = diaVencimento; }

    /** Devolve o valor da mensalidade. */
    public BigDecimal getValorMensalidade() { return valorMensalidade; }
    /** Define o valor da mensalidade. */
    public void setValorMensalidade(BigDecimal valor) { this.valorMensalidade = valor; }

    /** Devolve a data até a qual o contrato vale. */
    public LocalDate getVigenciaAte() { return vigenciaAte; }
    /** Define a data até a qual o contrato vale. */
    public void setVigenciaAte(LocalDate vigenciaAte) { this.vigenciaAte = vigenciaAte; }

    /** Devolve a data da assinatura. */
    public LocalDate getDataAssinatura() { return dataAssinatura; }
    /** Define a data da assinatura. */
    public void setDataAssinatura(LocalDate dataAssinatura) { this.dataAssinatura = dataAssinatura; }

    /** Devolve a data de saída. */
    public LocalDate getDataSaida() { return dataSaida; }
    /** Define a data de saída. */
    public void setDataSaida(LocalDate dataSaida) { this.dataSaida = dataSaida; }

    /** Devolve a versão do contrato. */
    public int getVersao() { return versao; }
    /** Define a versão do contrato. */
    public void setVersao(int versao) { this.versao = versao; }

    /** Devolve a situação. */
    public String getStatus() { return status; }
    /** Define a situação. */
    public void setStatus(String status) { this.status = status; }

    /** Texto amigável para a coluna "Assinatura" da tabela. */
    public String getSituacaoAssinatura() {
        return dataAssinatura == null ? "Aguardando assinatura" : "Assinado";
    }
}