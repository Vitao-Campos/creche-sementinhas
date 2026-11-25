package br.com.crechesementinhas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.crechesementinhas.model.AlunoTurma;
import br.com.crechesementinhas.model.SituacaoFaixaEtaria;
import br.com.crechesementinhas.model.Turma;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class TurmaValidadorTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 25);
    private static final LocalDate PRAZO = LocalDate.of(2027, 2, 28);

    /** Criança de 25 meses matriculada no Maternal I (12 a 23 meses): já passou da faixa. */
    private AlunoTurma aluno() {
        AlunoTurma a = new AlunoTurma();
        a.setIdAluno(1);
        a.setNome("Pedro Lima");
        a.setDataNascimento(LocalDate.of(2024, 10, 10));
        a.setIdMatricula(7);
        a.setNumeroMatricula("MAT-2026-007");
        a.setIdTurma(2);
        a.setNomeTurma("Maternal I");
        a.setIdadeMinimaMeses(12);
        a.setIdadeMaximaMeses(23);
        return a;
    }

    private Turma turma(int id, String nome, int min, int max, int capacidade, int ativas) {
        Turma t = new Turma();
        t.setIdTurma(id);
        t.setNome(nome);
        t.setIdadeMinimaMeses(min);
        t.setIdadeMaximaMeses(max);
        t.setCapacidade(capacidade);
        t.setMatriculasAtivas(ativas);
        return t;
    }

    // ---------- alerta de faixa etária

    @Test
    void classificaAFaixa() {
        assertEquals(SituacaoFaixaEtaria.ABAIXO, SituacaoFaixaEtaria.de(11, 12, 23));
        assertEquals(SituacaoFaixaEtaria.DENTRO, SituacaoFaixaEtaria.de(12, 12, 23));
        assertEquals(SituacaoFaixaEtaria.ULTIMO_MES, SituacaoFaixaEtaria.de(23, 12, 23));
        assertEquals(SituacaoFaixaEtaria.ACIMA, SituacaoFaixaEtaria.de(24, 12, 23));
    }

    @Test
    void ultimoMesAlertaMasNaoEstaForaDaFaixa() {
        assertTrue(SituacaoFaixaEtaria.ULTIMO_MES.isAlerta());
        assertTrue(!SituacaoFaixaEtaria.ULTIMO_MES.isForaDaFaixa());
    }

    @Test
    void criancaQueCresceuFicaAcimaDaFaixa() {
        assertEquals(SituacaoFaixaEtaria.ACIMA, aluno().situacaoFaixa(HOJE));
    }

    @Test
    void semMatriculaNaoTemAlerta() {
        AlunoTurma a = aluno();
        a.setIdMatricula(null);
        assertEquals(SituacaoFaixaEtaria.SEM_MATRICULA, a.situacaoFaixa(HOJE));
        assertTrue(!a.situacaoFaixa(HOJE).isAlerta());
    }

    // ---------- troca de turma

    @Test
    void trocaParaATurmaCertaEhValidaESemAviso() {
        Turma maternal2 = turma(3, "Maternal II", 24, 35, 16, 5);
        assertTrue(TurmaValidador.validarTroca(aluno(), maternal2, "Completou 2 anos").isEmpty());
        assertTrue(TurmaValidador.avisoFaixaNaNovaTurma(aluno(), maternal2, HOJE).isEmpty());
    }

    @Test
    void trocaParaTurmaForaDaFaixaAvisa() {
        Turma bercario = turma(1, "Berçário", 0, 11, 12, 0);
        assertTrue(TurmaValidador.avisoFaixaNaNovaTurma(aluno(), bercario, HOJE).orElseThrow().contains("25 meses"));
    }

    @Test
    void trocaParaAMesmaTurmaImpede() {
        List<String> erros = TurmaValidador.validarTroca(aluno(), turma(2, "Maternal I", 12, 23, 14, 3), "x");
        assertEquals(1, erros.size());
        assertTrue(erros.get(0).contains("já está"));
    }

    @Test
    void trocaParaTurmaLotadaImpede() {
        assertTrue(TurmaValidador.validarTroca(aluno(), turma(3, "Maternal II", 24, 35, 16, 16), "x")
                .get(0).contains("lotada"));
    }

    @Test
    void trocaSemMotivoImpede() {
        assertEquals(1, TurmaValidador.validarTroca(aluno(), turma(3, "Maternal II", 24, 35, 16, 0), "  ").size());
    }

    @Test
    void trocaSemMatriculaAtivaImpede() {
        AlunoTurma a = aluno();
        a.setIdMatricula(null);
        assertTrue(TurmaValidador.validarTroca(a, turma(3, "Maternal II", 24, 35, 16, 0), "x")
                .get(0).contains("ATIVA"));
    }

    // ---------- andarilha

    @Test
    void marcarAndarilhaComMotivoEhValido() {
        assertTrue(TurmaValidador.validarAndarilha(aluno(), true, "Frequenta duas turmas", PRAZO, HOJE).isEmpty());
    }

    @Test
    void marcarAndarilhaSemMotivoImpede() {
        assertEquals(1, TurmaValidador.validarAndarilha(aluno(), true, " ", PRAZO, HOJE).size());
    }

    @Test
    void semMudancaNaoGrava() {
        AlunoTurma a = aluno();
        a.setAndarilha(true);
        a.setMotivoAndarilha("Frequenta duas turmas");
        a.setAndarilhaAte(PRAZO);
        assertTrue(TurmaValidador.validarAndarilha(a, true, " Frequenta duas turmas ", PRAZO, HOJE)
                .get(0).contains("Nada mudou"));
    }

    @Test
    void desmarcarAndarilhaEhValido() {
        AlunoTurma a = aluno();
        a.setAndarilha(true);
        a.setMotivoAndarilha("Frequenta duas turmas");
        assertTrue(TurmaValidador.validarAndarilha(a, false, "", null, HOJE).isEmpty());
    }

    // ---------- andarilha x alerta de faixa (RN-08) e prazo (RF-11)

    private AlunoTurma andarilha(LocalDate ate) {
        AlunoTurma a = aluno(); // 25 meses no Maternal I (12 a 23): fora da faixa pela idade
        a.setAndarilha(true);
        a.setMotivoAndarilha("Adaptação com o irmão");
        a.setAndarilhaAte(ate);
        return a;
    }

    @Test
    void andarilhaNoPrazoNaoAlertaENaoContaComoFora() {
        SituacaoFaixaEtaria s = andarilha(PRAZO).situacaoFaixa(HOJE);
        assertEquals(SituacaoFaixaEtaria.ANDARILHA, s);
        assertTrue(!s.isAlerta() && !s.isForaDaFaixa());
    }

    @Test
    void andarilhaValeAteOUltimoDiaDoPrazo() {
        assertEquals(SituacaoFaixaEtaria.ANDARILHA, andarilha(HOJE).situacaoFaixa(HOJE));
    }

    @Test
    void andarilhaVencidaVoltaAAlertarEContaComoFora() {
        SituacaoFaixaEtaria s = andarilha(HOJE.minusDays(1)).situacaoFaixa(HOJE);
        assertEquals(SituacaoFaixaEtaria.ANDARILHA_VENCIDA, s);
        assertTrue(s.isAlerta() && s.isForaDaFaixa());
    }

    @Test
    void andarilhaAntigaSemPrazoContaComoVencida() {
        assertEquals(SituacaoFaixaEtaria.ANDARILHA_VENCIDA, andarilha(null).situacaoFaixa(HOJE));
    }

    @Test
    void marcarAndarilhaSemPrazoImpede() {
        assertTrue(TurmaValidador.validarAndarilha(aluno(), true, "Motivo", null, HOJE).get(0).contains("prazo"));
    }

    @Test
    void marcarAndarilhaComPrazoNoPassadoImpede() {
        assertEquals(1, TurmaValidador.validarAndarilha(aluno(), true, "Motivo", HOJE.minusDays(3), HOJE).size());
    }

    @Test
    void renovarSoOPrazoEhUmaMudancaValida() {
        AlunoTurma a = andarilha(HOJE.minusDays(1)); // vencida
        assertTrue(TurmaValidador.validarAndarilha(a, true, "Adaptação com o irmão", PRAZO, HOJE).isEmpty());
    }
}
