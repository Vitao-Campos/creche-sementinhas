package br.com.crechesementinhas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.crechesementinhas.model.FaixaMensalidade;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class CalculadoraMensalidadeTest {

    private static final List<FaixaMensalidade> FAIXAS = List.of(
            new FaixaMensalidade(1, "Berçário", 0, 0, new BigDecimal("850.00")),
            new FaixaMensalidade(2, "Maternal I", 1, 1, new BigDecimal("780.00")),
            new FaixaMensalidade(3, "Maternal II", 2, 2, new BigDecimal("720.00")),
            new FaixaMensalidade(4, "Jardim", 3, null, new BigDecimal("680.00")));

    private static int idade(String nascimento, String referencia) {
        return CalculadoraMensalidade.idadeEmAnosParaMensalidade(
                LocalDate.parse(nascimento), LocalDate.parse(referencia));
    }

    @Test
    void bebeDeOitoMesesTemZeroAnos() {
        assertEquals(0, idade("2026-03-10", "2026-11-24"));
    }

    @Test
    void noMesDoAniversarioJaContaOAnoNovoMesmoAntesDoDia() {
        // RN-07: reajuste no mês do aniversário, desde o dia 1º
        assertEquals(1, idade("2025-11-28", "2026-11-01"));
    }

    @Test
    void noMesAnteriorAoAniversarioAindaNaoMuda() {
        assertEquals(0, idade("2025-11-28", "2026-10-31"));
    }

    @Test
    void nascimentoNoFuturoDaZero() {
        assertEquals(0, idade("2027-01-01", "2026-11-24"));
    }

    @Test
    void escolheAFaixaPelaIdade() {
        assertEquals("Berçário", CalculadoraMensalidade.faixaPara(FAIXAS, 0).orElseThrow().getDescricao());
        assertEquals("Maternal II", CalculadoraMensalidade.faixaPara(FAIXAS, 2).orElseThrow().getDescricao());
    }

    @Test
    void faixaSemLimiteSuperiorAceitaQualquerIdadeAcima() {
        assertEquals("Jardim", CalculadoraMensalidade.faixaPara(FAIXAS, 5).orElseThrow().getDescricao());
    }

    @Test
    void semFaixaParaAIdadeDevolveVazio() {
        assertTrue(CalculadoraMensalidade.faixaPara(FAIXAS.subList(0, 2), 4).isEmpty());
    }

    @Test
    void proporcionalDeDezDiasEmNovembro() {
        // 780 × 10 ÷ 30 = 260,00
        assertEquals(new BigDecimal("260.00"),
                CalculadoraMensalidade.proporcional(new BigDecimal("780.00"), LocalDate.of(2026, 11, 10)));
    }

    @Test
    void proporcionalArredondaCentavos() {
        // 850 × 15 ÷ 31 = 411,290... -> 411,29
        assertEquals(new BigDecimal("411.29"),
                CalculadoraMensalidade.proporcional(new BigDecimal("850.00"), LocalDate.of(2026, 12, 15)));
    }

    @Test
    void ultimoDiaDoMesCobraOMesInteiro() {
        assertEquals(new BigDecimal("720.00"),
                CalculadoraMensalidade.proporcional(new BigDecimal("720.00"), LocalDate.of(2028, 2, 29)));
    }

    @Test
    void proporcionalRecusaMensalidadeZero() {
        assertThrows(IllegalArgumentException.class,
                () -> CalculadoraMensalidade.proporcional(BigDecimal.ZERO, LocalDate.of(2026, 11, 10)));
    }

    @Test
    void metadeDoMesCobraMetade() {
        // sugestão da revisão: dia 15 de um mês de 30 dias = metade do valor
        assertEquals(new BigDecimal("390.00"),
                CalculadoraMensalidade.proporcional(new BigDecimal("780.00"), LocalDate.of(2026, 11, 15)));
    }

    @Test
    void avisoDe29DiasEhCurtoE30NaoEh() {
        LocalDate hoje = LocalDate.of(2026, 11, 24);
        assertEquals(29, CalculadoraMensalidade.diasDeAviso(hoje, LocalDate.of(2026, 12, 23)));
        assertTrue(CalculadoraMensalidade.avisoCurto(29));
        assertTrue(!CalculadoraMensalidade.avisoCurto(30));
    }

    @Test
    void saidaNoPassadoContaZeroDiasDeAviso() {
        assertEquals(0, CalculadoraMensalidade.diasDeAviso(LocalDate.of(2026, 11, 24), LocalDate.of(2026, 11, 20)));
    }

    @Test
    void dataMinimaDeDesligamentoEhHojeMais30DiasCorridos() {
        assertEquals(LocalDate.of(2026, 12, 24),
                CalculadoraMensalidade.dataMinimaDesligamento(LocalDate.of(2026, 11, 24)));
    }
}
