package br.com.crechesementinhas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resultado do cálculo do desligamento (RF-10 / RN-12): quanto é cobrado no último mês.
 *
 * @param dataDesligamento    último dia em que a criança frequenta
 * @param mensalidadeBase     mensalidade usada no cálculo (a do contrato vigente ou a da tabela)
 * @param origemMensalidade   "contrato vigente" ou "tabela por faixa etária"
 * @param diasCobrados        dias do mês até a data de desligamento (inclusive)
 * @param diasNoMes           quantidade de dias do mês do desligamento
 * @param valorProporcional   valor a cobrar no mês do desligamento
 * @param diasDeAviso         dias entre a comunicação e o desligamento
 */
public record Desligamento(
        LocalDate dataDesligamento,
        BigDecimal mensalidadeBase,
        String origemMensalidade,
        int diasCobrados,
        int diasNoMes,
        BigDecimal valorProporcional,
        long diasDeAviso) {
}
