package br.com.crechesementinhas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.crechesementinhas.model.Aluno;
import br.com.crechesementinhas.model.Matricula;
import br.com.crechesementinhas.model.Sexo;
import br.com.crechesementinhas.model.Turma;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class MatriculaValidadorTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 24);

    private Aluno aluno() {
        Aluno a = new Aluno();
        a.setIdAluno(1);
        a.setNome("Maria Souza");
        a.setDataNascimento(LocalDate.of(2025, 6, 10)); // 17 meses em 24/11/2026
        a.setSexo(Sexo.F);
        return a;
    }

    private Turma turma(int min, int max, int capacidade, int ativas) {
        Turma t = new Turma();
        t.setIdTurma(2);
        t.setNome("Maternal I");
        t.setIdadeMinimaMeses(min);
        t.setIdadeMaximaMeses(max);
        t.setCapacidade(capacidade);
        t.setMatriculasAtivas(ativas);
        return t;
    }

    private static final LocalDate PRAZO = HOJE.plusMonths(3);

    private List<String> validar(Aluno a, Turma t, boolean andarilha, String motivo, boolean temAtiva) {
        return MatriculaValidador.validarEfetivacao(a, t, HOJE, andarilha, motivo,
                andarilha ? PRAZO : null, temAtiva, HOJE);
    }

    @Test
    void andarilhaComMotivoEPrazoEhValida() {
        assertTrue(validar(aluno(), turma(12, 23, 14, 0), true, "Frequenta duas turmas", false).isEmpty());
    }

    @Test
    void andarilhaSemPrazoImpede() {
        // RF-11: tempo previsto de permanência é obrigatório
        List<String> erros = MatriculaValidador.validarEfetivacao(aluno(), turma(12, 23, 14, 0), HOJE,
                true, "Frequenta duas turmas", null, false, HOJE);
        assertEquals(1, erros.size());
        assertTrue(erros.get(0).contains("prazo"));
    }

    @Test
    void andarilhaComPrazoHojeImpede() {
        assertEquals(1, MatriculaValidador.validarEfetivacao(aluno(), turma(12, 23, 14, 0), HOJE,
                true, "Frequenta duas turmas", HOJE, false, HOJE).size());
    }

    @Test
    void matriculaCompletaEhValida() {
        assertTrue(validar(aluno(), turma(12, 23, 14, 3), false, null, false).isEmpty());
    }

    @Test
    void turmaLotadaImpede() {
        List<String> erros = validar(aluno(), turma(12, 23, 14, 14), false, null, false);
        assertEquals(1, erros.size());
        assertTrue(erros.get(0).contains("lotada"));
    }

    @Test
    void criancaComMatriculaAtivaNaoMatriculaDeNovo() {
        assertTrue(validar(aluno(), turma(12, 23, 14, 0), false, null, true).get(0).contains("ATIVA"));
    }

    @Test
    void andarilhaSemMotivoImpede() {
        assertEquals(1, validar(aluno(), turma(12, 23, 14, 0), true, "   ", false).size());
    }

    @Test
    void semTurmaESemCriancaDaDoisErros() {
        assertEquals(2, validar(null, null, false, null, false).size());
    }

    @Test
    void dataAntesDoNascimentoImpede() {
        List<String> erros = MatriculaValidador.validarEfetivacao(aluno(), turma(0, 23, 14, 0),
                LocalDate.of(2025, 1, 1), false, null, null, false, HOJE);
        assertTrue(erros.get(0).contains("nascimento"));
    }

    @Test
    void idadeDentroDaFaixaNaoAvisa() {
        assertTrue(MatriculaValidador.avisoFaixaEtaria(aluno(), turma(12, 23, 14, 0), HOJE).isEmpty());
    }

    @Test
    void idadeForaDaFaixaAvisa() {
        assertTrue(MatriculaValidador.avisoFaixaEtaria(aluno(), turma(0, 11, 14, 0), HOJE)
                .orElseThrow().contains("17 meses"));
    }

    private Matricula ativa() {
        Matricula m = new Matricula();
        m.setIdMatricula(10);
        m.setStatus(Matricula.ATIVA);
        m.setDataMatricula(LocalDate.of(2026, 2, 1));
        return m;
    }

    @Test
    void desligamentoComTrintaDiasEhValido() {
        assertTrue(MatriculaValidador.validarDesligamento(ativa(), HOJE.plusDays(30), "Mudança", HOJE).isEmpty());
    }

    @Test
    void avisoCurtoNaoEhErroDeDados() {
        // RN-12: o aviso curto é tratado no service (confirmação + auditoria), não como erro
        assertTrue(MatriculaValidador.validarDesligamento(ativa(), HOJE.plusDays(5), "Mudança", HOJE).isEmpty());
    }

    @Test
    void desligamentoDeMatriculaEncerradaImpede() {
        Matricula m = ativa();
        m.setStatus(Matricula.ENCERRADA);
        assertEquals(1, MatriculaValidador.validarDesligamento(m, HOJE.plusDays(30), "Mudança", HOJE).size());
    }

    @Test
    void desligamentoSemMotivoESemDataDaDoisErros() {
        assertEquals(2, MatriculaValidador.validarDesligamento(ativa(), null, " ", HOJE).size());
    }
}
