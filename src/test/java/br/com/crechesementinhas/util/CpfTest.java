package br.com.crechesementinhas.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CpfTest {

    @Test
    void aceitaCpfValidoComESemMascara() {
        assertTrue(Cpf.valido("52998224725"));
        assertTrue(Cpf.valido("529.982.247-25"));
    }

    @Test
    void recusaDigitoVerificadorErrado() {
        assertFalse(Cpf.valido("52998224726"));
    }

    @Test
    void recusaSequenciaRepetida() {
        assertFalse(Cpf.valido("111.111.111-11"));
    }

    @Test
    void recusaTamanhoErradoOuNulo() {
        assertFalse(Cpf.valido("1234"));
        assertFalse(Cpf.valido(null));
    }

    @Test
    void normalizarTiraMascaraEDevolveNuloQuandoVazio() {
        assertEquals("52998224725", Cpf.normalizar("529.982.247-25"));
        assertNull(Cpf.normalizar("   "));
        assertNull(Cpf.normalizar(null));
    }

    @Test
    void formataComPontosETraco() {
        assertEquals("529.982.247-25", Cpf.formatar("52998224725"));
    }
}
