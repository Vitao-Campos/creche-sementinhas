package br.com.crechesementinhas.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CalculadoraIdadeTest {

    private static final LocalDate NASCIMENTO = LocalDate.of(2024, 3, 15);

    @Test
    void umDiaAntesDoDiaDoMesAindaNaoCompletaOMes() {
        assertEquals(11, CalculadoraIdade.emMeses(NASCIMENTO, LocalDate.of(2025, 3, 14)));
    }

    @Test
    void noDiaDoMesCompletaOMes() {
        assertEquals(12, CalculadoraIdade.emMeses(NASCIMENTO, LocalDate.of(2025, 3, 15)));
    }

    @Test
    void nascidoEm29DeFevereiroCompletaOAnoEm1DeMarco() {
        LocalDate bissexto = LocalDate.of(2024, 2, 29);
        assertEquals(11, CalculadoraIdade.emMeses(bissexto, LocalDate.of(2025, 2, 28)));
        assertEquals(12, CalculadoraIdade.emMeses(bissexto, LocalDate.of(2025, 3, 1)));
    }

    @Test
    void nascimentoNoFuturoDaZero() {
        assertEquals(0, CalculadoraIdade.emMeses(LocalDate.of(2030, 1, 1), LocalDate.of(2026, 1, 1)));
    }

    @Test
    void descreveEmAnosEMeses() {
        assertEquals("1 ano e 3 meses", CalculadoraIdade.descrever(NASCIMENTO, LocalDate.of(2025, 6, 15)));
        assertEquals("2 anos", CalculadoraIdade.descrever(NASCIMENTO, LocalDate.of(2026, 3, 15)));
        assertEquals("8 meses", CalculadoraIdade.descrever(NASCIMENTO, LocalDate.of(2024, 11, 15)));
        assertEquals("1 mês", CalculadoraIdade.descrever(NASCIMENTO, LocalDate.of(2024, 4, 15)));
        assertEquals("menos de 1 mês", CalculadoraIdade.descrever(NASCIMENTO, LocalDate.of(2024, 3, 20)));
    }
}
