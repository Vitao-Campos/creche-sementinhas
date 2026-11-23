package br.com.crechesementinhas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.crechesementinhas.model.Aluno;
import br.com.crechesementinhas.model.Sexo;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class AlunoValidadorTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 21);

    private Aluno alunoValido() {
        Aluno aluno = new Aluno();
        aluno.setNome("Maria Souza");
        aluno.setDataNascimento(LocalDate.of(2024, 5, 10));
        aluno.setSexo(Sexo.F);
        return aluno;
    }

    private List<String> validar(Aluno aluno) {
        AlunoValidador.normalizar(aluno);
        return AlunoValidador.validar(aluno, HOJE);
    }

    @Test
    void alunoCompletoSemCpfEhValido() {
        assertTrue(validar(alunoValido()).isEmpty());
    }

    @Test
    void cpfValidoComMascaraEhAceitoEGuardadoSoComDigitos() {
        Aluno aluno = alunoValido();
        aluno.setCpf("529.982.247-25");
        assertTrue(validar(aluno).isEmpty());
        assertEquals("52998224725", aluno.getCpf());
    }

    @Test
    void cpfEmBrancoViraNuloEContinuaValido() {
        Aluno aluno = alunoValido();
        aluno.setCpf("  ");
        assertTrue(validar(aluno).isEmpty());
        assertNull(aluno.getCpf());
    }

    @Test
    void cpfInvalidoEhRecusado() {
        Aluno aluno = alunoValido();
        aluno.setCpf("123.456.789-00");
        assertEquals(List.of("CPF inválido."), validar(aluno));
    }

    @Test
    void nomeEmBrancoEhRecusado() {
        Aluno aluno = alunoValido();
        aluno.setNome("   ");
        assertEquals(List.of("Informe o nome da criança."), validar(aluno));
    }

    @Test
    void nomeEhLimpoNasPontas() {
        Aluno aluno = alunoValido();
        aluno.setNome("  Maria Souza  ");
        validar(aluno);
        assertEquals("Maria Souza", aluno.getNome());
    }

    @Test
    void nascimentoNoFuturoEhRecusado() {
        Aluno aluno = alunoValido();
        aluno.setDataNascimento(HOJE.plusDays(1));
        assertEquals(List.of("A data de nascimento não pode ser no futuro."), validar(aluno));
    }

    @Test
    void nascimentoHojeEhAceito() {
        Aluno aluno = alunoValido();
        aluno.setDataNascimento(HOJE);
        assertTrue(validar(aluno).isEmpty());
    }

    @Test
    void semDataNemSexoAcusaOsDois() {
        Aluno aluno = alunoValido();
        aluno.setDataNascimento(null);
        aluno.setSexo(null);
        assertEquals(List.of("Informe a data de nascimento.", "Informe o sexo."), validar(aluno));
    }

    @Test
    void nomeMuitoLongoEhRecusado() {
        Aluno aluno = alunoValido();
        aluno.setNome("A".repeat(101));
        assertEquals(1, validar(aluno).size());
    }
}
