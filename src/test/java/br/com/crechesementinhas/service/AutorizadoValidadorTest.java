package br.com.crechesementinhas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.crechesementinhas.model.AlunoPessoaAutorizada;
import br.com.crechesementinhas.model.TipoAutorizacao;
import java.util.List;
import org.junit.jupiter.api.Test;

class AutorizadoValidadorTest {

    private AlunoPessoaAutorizada valido() {
        AlunoPessoaAutorizada a = new AlunoPessoaAutorizada();
        a.setIdAluno(1);
        a.setNome("  João Pereira ");
        a.setRg("12.345.678-9");
        a.setTelefone("(51) 3234-5678");
        a.setTipoAutorizacao(TipoAutorizacao.AMBOS);
        return a;
    }

    private List<String> erros(AlunoPessoaAutorizada a) {
        AutorizadoValidador.normalizar(a);
        return AutorizadoValidador.validar(a);
    }

    @Test
    void dadosValidosPassamEParentescoEhOpcional() {
        assertTrue(erros(valido()).isEmpty());
    }

    @Test
    void normalizarApara() {
        AlunoPessoaAutorizada a = valido();
        a.setParentesco("   ");
        AutorizadoValidador.normalizar(a);
        assertEquals("João Pereira", a.getNome());
        assertEquals("5132345678", a.getTelefone());
        assertNull(a.getParentesco());
    }

    @Test
    void obrigatorios() {
        // criança, nome, RG, telefone e tipo
        assertEquals(5, erros(new AlunoPessoaAutorizada()).size());
    }

    @Test
    void telefoneInvalido() {
        AlunoPessoaAutorizada a = valido();
        a.setTelefone("1234");
        assertEquals(1, erros(a).size());
    }

    @Test
    void limites() {
        AlunoPessoaAutorizada a = valido();
        a.setNome("a".repeat(AutorizadoValidador.MAX_NOME + 1));
        a.setRg("1".repeat(AutorizadoValidador.MAX_RG + 1));
        a.setParentesco("a".repeat(AutorizadoValidador.MAX_PARENTESCO + 1));
        assertEquals(3, erros(a).size());
    }
}
