package br.com.crechesementinhas.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MascaraTest {

    @Test
    void cpfComecaSemSeparadorEVaiCompletando() {
        assertEquals("123", Mascara.formatarCpf("123"));
        assertEquals("123.4", Mascara.formatarCpf("1234"));
        assertEquals("123.456.789-01", Mascara.formatarCpf("12345678901"));
    }

    @Test
    void telefoneFixoECelular() {
        assertEquals("(11", Mascara.formatarTelefone("11"));
        assertEquals("(11) 3234-5678", Mascara.formatarTelefone("1132345678"));
        assertEquals("(11) 91234-5678", Mascara.formatarTelefone("11912345678"));
    }

    @Test
    void dataVaiCompletandoComBarras() {
        assertEquals("12", Mascara.formatarData("12"));
        assertEquals("12/0", Mascara.formatarData("120"));
        assertEquals("12/05/2", Mascara.formatarData("12052"));
        assertEquals("12/05/2022", Mascara.formatarData("12052022"));
    }

    @Test
    void cepComTraco() {
        assertEquals("12345", Mascara.formatarCep("12345"));
        assertEquals("12345-678", Mascara.formatarCep("12345678"));
    }
}
