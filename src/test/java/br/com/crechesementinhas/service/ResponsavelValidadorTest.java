package br.com.crechesementinhas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.crechesementinhas.model.AlunoResponsavel;
import br.com.crechesementinhas.model.Responsavel;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ResponsavelValidadorTest {

    // CPF matematicamente válido (gerado pelo algoritmo, não é de pessoa real).
    private static final String CPF_VALIDO = "529.982.247-25";

    private Responsavel valido() {
        Responsavel r = new Responsavel();
        r.setNome("  Maria Souza ");
        r.setCpf(CPF_VALIDO);
        r.setTelefone("(51) 91234-5678");
        return r;
    }

    private List<String> erros(Responsavel r) {
        ResponsavelValidador.normalizar(r);
        return ResponsavelValidador.validar(r);
    }

    @Test
    void dadosMinimosValidosPassam() {
        assertTrue(erros(valido()).isEmpty());
    }

    @Test
    void normalizarTiraMascaraEEspacos() {
        Responsavel r = valido();
        r.setCep("90.010-000");
        r.setEmail("   ");
        ResponsavelValidador.normalizar(r);
        assertEquals("Maria Souza", r.getNome());
        assertEquals("52998224725", r.getCpf());
        assertEquals("51912345678", r.getTelefone());
        assertEquals("90010000", r.getCep());
        assertNull(r.getEmail());
    }

    @Test
    void nomeCpfETelefoneSaoObrigatorios() {
        List<String> erros = erros(new Responsavel());
        assertEquals(3, erros.size());
    }

    @Test
    void cpfInvalidoEhRecusado() {
        Responsavel r = valido();
        r.setCpf("111.111.111-11");
        assertTrue(erros(r).contains("CPF inválido."));
    }

    @Test
    void telefoneComTamanhoErradoEhRecusado() {
        Responsavel r = valido();
        r.setTelefone("1234-5678");
        assertEquals(1, erros(r).size());
    }

    @Test
    void emailInvalidoEhRecusadoEOpcionalPassa() {
        Responsavel r = valido();
        r.setEmail("maria@");
        assertTrue(erros(r).contains("E-mail inválido."));

        Responsavel ok = valido();
        ok.setEmail("maria@exemplo.com");
        assertTrue(erros(ok).isEmpty());
    }

    @Test
    void cepPrecisaTer8Digitos() {
        Responsavel r = valido();
        r.setCep("9001");
        assertEquals(1, erros(r).size());
    }

    @Test
    void limitesDeTamanho() {
        Responsavel r = valido();
        r.setNome("a".repeat(ResponsavelValidador.MAX_NOME + 1));
        r.setEndereco("a".repeat(ResponsavelValidador.MAX_ENDERECO + 1));
        r.setProfissao("a".repeat(ResponsavelValidador.MAX_PROFISSAO + 1));
        assertEquals(3, erros(r).size());
    }

    // ---------------------------------------------------------- vínculo

    private AlunoResponsavel vinculoValido() {
        AlunoResponsavel v = new AlunoResponsavel();
        v.setIdAluno(1);
        v.setGrauParentesco(" Mãe ");
        v.setDataInicio(LocalDate.of(2026, 2, 1));
        return v;
    }

    private List<String> errosVinculo(AlunoResponsavel v) {
        ResponsavelValidador.normalizarVinculo(v);
        return ResponsavelValidador.validarVinculo(v);
    }

    @Test
    void vinculoValidoPassaEAparaOParentesco() {
        AlunoResponsavel v = vinculoValido();
        assertTrue(errosVinculo(v).isEmpty());
        assertEquals("Mãe", v.getGrauParentesco());
    }

    @Test
    void vinculoExigeCriancaParentescoEInicio() {
        assertEquals(3, errosVinculo(new AlunoResponsavel()).size());
    }

    @Test
    void fimAnteriorAoInicioEhRecusado() {
        AlunoResponsavel v = vinculoValido();
        v.setDataFim(LocalDate.of(2026, 1, 31));
        assertEquals(1, errosVinculo(v).size());
    }

    @Test
    void fimIgualAoInicioEhAceito() {
        AlunoResponsavel v = vinculoValido();
        v.setDataFim(v.getDataInicio());
        assertTrue(errosVinculo(v).isEmpty());
    }

    @Test
    void vigenciaDoVinculo() {
        AlunoResponsavel v = vinculoValido();
        assertTrue(v.estaVigente(LocalDate.of(2026, 5, 1)));
        assertTrue(!v.estaVigente(LocalDate.of(2026, 1, 31)));
        v.setDataFim(LocalDate.of(2026, 6, 30));
        assertTrue(v.estaVigente(LocalDate.of(2026, 6, 30)));
        assertTrue(!v.estaVigente(LocalDate.of(2026, 7, 1)));
    }
}
