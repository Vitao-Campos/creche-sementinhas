package br.com.crechesementinhas.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TelefoneTest {

    @Test
    void normalizarDeixaSoDigitos() {
        assertEquals("11912345678", Telefone.normalizar("(11) 91234-5678"));
        assertNull(Telefone.normalizar("  "));
        assertNull(Telefone.normalizar(null));
    }

    @Test
    void validaCelularEFixo() {
        assertTrue(Telefone.valido("(11) 91234-5678"));
        assertTrue(Telefone.valido("1132345678"));
        assertFalse(Telefone.valido("32345678"));
        assertFalse(Telefone.valido("519123456789"));
    }

    @Test
    void formata() {
        assertEquals("(11) 91234-5678", Telefone.formatar("11912345678"));
        assertEquals("(11) 3234-5678", Telefone.formatar("1132345678"));
        assertEquals("123", Telefone.formatar("123"));
        assertEquals("", Telefone.formatar(null));
    }
}
