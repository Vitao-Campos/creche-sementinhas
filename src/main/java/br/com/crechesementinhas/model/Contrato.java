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

    public int getIdContrato() { return idContrato; }
    public void setIdContrato(int idContrato) { this.idContrato = idContrato; }

    public int getIdMatricula() { return idMatricula; }
    public void setIdMatricula(int idMatricula) { this.idMatricula = idMatricula; }

    public int getIdResponsavelFinanceiro() { return idResponsavelFinanceiro; }
    public void setIdResponsavelFinanceiro(int id) { this.idResponsavelFinanceiro = id; }

    public String getNomeResponsavelFinanceiro() { return nomeResponsavelFinanceiro; }
    public void setNomeResponsavelFinanceiro(String nome) { this.nomeResponsavelFinanceiro = nome; }

    public String getPlano() { return plano; }
    public void setPlano(String plano) { this.plano = plano; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public int getDiaVencimento() { return diaVencimento; }
    public void setDiaVencimento(int diaVencimento) { this.diaVencimento = diaVencimento; }

    public BigDecimal getValorMensalidade() { return valorMensalidade; }
    public void setValorMensalidade(BigDecimal valor) { this.valorMensalidade = valor; }

    public LocalDate getVigenciaAte() { return vigenciaAte; }
    public void setVigenciaAte(LocalDate vigenciaAte) { this.vigenciaAte = vigenciaAte; }

    public LocalDate getDataAssinatura() { return dataAssinatura; }
    public void setDataAssinatura(LocalDate dataAssinatura) { this.dataAssinatura = dataAssinatura; }

    public LocalDate getDataSaida() { return dataSaida; }
    public void setDataSaida(LocalDate dataSaida) { this.dataSaida = dataSaida; }

    public int getVersao() { return versao; }
    public void setVersao(int versao) { this.versao = versao; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /** Texto amigável para a coluna "Assinatura" da tabela. */
    public String getSituacaoAssinatura() {
        return dataAssinatura == null ? "Aguardando assinatura" : "Assinado";
    }
}