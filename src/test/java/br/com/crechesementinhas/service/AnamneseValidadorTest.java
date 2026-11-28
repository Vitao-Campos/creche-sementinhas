package br.com.crechesementinhas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.crechesementinhas.model.FichaAnamnese;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnamneseValidadorTest {

    @Test
    void fichaTodaEmNaoEValida() {
        assertTrue(AnamneseValidador.validar(new FichaAnamnese()).isEmpty());
    }

    @Test
    void simSemDescricaoEhRecusado() {
        FichaAnamnese f = new FichaAnamnese();
        f.setPossuiAlergia(true);
        f.setDescricaoAlergia("   ");
        AnamneseValidador.normalizar(f);
        List<String> erros = AnamneseValidador.validar(f);
        assertEquals(1, erros.size());
        assertTrue(erros.get(0).contains("alergia"));
    }

    @Test
    void simComDescricaoEhAceitoEApara() {
        FichaAnamnese f = new FichaAnamnese();
        f.setUsaMedicacaoContinua(true);
        f.setDescricaoMedicacao("  Ritalina 10mg  ");
        AnamneseValidador.normalizar(f);
        assertEquals("Ritalina 10mg", f.getDescricaoMedicacao());
        assertTrue(AnamneseValidador.validar(f).isEmpty());
    }

    @Test
    void naoDescartaDescricaoEsquecida() {
        FichaAnamnese f = new FichaAnamnese();
        f.setPossuiCirurgia(false);
        f.setDescricaoCirurgia("apendicite");
        AnamneseValidador.normalizar(f);
        assertNull(f.getDescricaoCirurgia());
    }

    @Test
    void descricaoMuitoLongaEhRecusada() {
        FichaAnamnese f = new FichaAnamnese();
        f.setPossuiFraturas(true);
        f.setDescricaoFratura("x".repeat(256));
        AnamneseValidador.normalizar(f);
        assertEquals(1, AnamneseValidador.validar(f).size());
    }

    @Test
    void antitermicoEhOpcionalMasTemLimite() {
        FichaAnamnese f = new FichaAnamnese();
        f.setReceitaAntitermico("  ");
        AnamneseValidador.normalizar(f);
        assertNull(f.getReceitaAntitermico());
        f.setReceitaAntitermico("y".repeat(256));
        assertEquals(1, AnamneseValidador.validar(f).size());
    }
}
