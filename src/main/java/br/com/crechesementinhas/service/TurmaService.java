package br.com.crechesementinhas.service;

import br.com.crechesementinhas.dao.AlunoTurmaDAO;
import br.com.crechesementinhas.dao.MatriculaDAO;
import br.com.crechesementinhas.dao.TurmaDAO;
import br.com.crechesementinhas.model.AlunoTurma;
import br.com.crechesementinhas.model.Matricula;
import br.com.crechesementinhas.model.Permissao;
import br.com.crechesementinhas.model.Turma;
import br.com.crechesementinhas.util.Transacao;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Frente E: consulta de alunos por turma com alerta de faixa etária,
 * troca de turma (só com confirmação) e registro de andarilha.
 * Toda alteração é auditada (RNF-02) e exige permissão (RNF-03).
 */
public class TurmaService {

    private final TurmaDAO turmaDAO = new TurmaDAO();
    private final AlunoTurmaDAO alunoTurmaDAO = new AlunoTurmaDAO();
    private final MatriculaDAO matriculaDAO = new MatriculaDAO();
    private final AuditoriaService auditoria = new AuditoriaService();

    /** Turmas com a ocupação atual (matrículas ATIVAS). */
    public List<Turma> listarTurmas() throws SQLException {
        return turmaDAO.listar();
    }

    /**
     * Busca de alunos para a tabela da tela.
     *
     * @param somenteComAlerta só quem está fora da faixa da turma ou no último mês dela
     */
    public List<AlunoTurma> buscar(String trechoNome, Turma turma, boolean somenteAtivos,
                                   boolean somenteComAlerta) throws SQLException {
        Integer idTurma = turma == null ? null : turma.getIdTurma();
        List<AlunoTurma> lista = alunoTurmaDAO.buscar(trechoNome, idTurma, somenteAtivos || somenteComAlerta);
        if (somenteComAlerta) {
            LocalDate hoje = LocalDate.now();
            lista.removeIf(a -> !a.situacaoFaixa(hoje).isAlerta());
        }
        return lista;
    }

    /** Quantas crianças com matrícula ativa estão FORA da faixa da própria turma hoje. */
    public long contarForaDaFaixa(List<AlunoTurma> alunos) {
        LocalDate hoje = LocalDate.now();
        return alunos.stream().filter(a -> a.situacaoFaixa(hoje).isForaDaFaixa()).count();
    }

    /** Aviso para a tela pedir confirmação antes de trocar para uma turma fora da faixa. */
    public Optional<String> avisoFaixaNaNovaTurma(AlunoTurma aluno, Turma nova) {
        return TurmaValidador.avisoFaixaNaNovaTurma(aluno, nova, LocalDate.now());
    }

    /**
     * Troca a criança de turma. A tela sempre pede confirmação antes de chamar.
     *
     * @param confirmouForaDaFaixa o usuário confirmou o aviso de faixa etária da nova turma (se houve)
     */
    public void trocarTurma(AlunoTurma aluno, Turma nova, String justificativa,
                            boolean confirmouForaDaFaixa) throws SQLException {
        ControleAcesso.exigir(Permissao.CONFIRMAR_MATRICULA); // RNF-03
        exigirSemErros(TurmaValidador.validarTroca(aluno, nova, justificativa));

        Transacao.executar(conexao -> {
            // Confere de novo com as linhas TRAVADAS: vale o que está no banco agora
            Matricula atual = matriculaDAO.buscarETravar(conexao, aluno.getIdMatricula())
                    .filter(Matricula::isAtiva)
                    .orElseThrow(() -> new MatriculaException(
                            "A matrícula não está mais ATIVA. Atualize a lista."));
            if (!Objects.equals(atual.getIdTurma(), aluno.getIdTurma())) {
                throw new MatriculaException("A turma desta criança foi alterada por outro usuário. "
                        + "Atualize a lista.");
            }
            Turma travada = turmaDAO.buscarETravar(conexao, nova.getIdTurma())
                    .orElseThrow(() -> new MatriculaException("Turma não encontrada."));
            exigirSemErros(TurmaValidador.validarTroca(aluno, travada, justificativa));

            Optional<String> aviso = TurmaValidador.avisoFaixaNaNovaTurma(aluno, travada, LocalDate.now());
            if (aviso.isPresent() && !confirmouForaDaFaixa) {
                throw new MatriculaException(aviso.get() + " Confirme para trocar mesmo assim.");
            }

            matriculaDAO.trocarTurma(conexao, atual.getIdMatricula(), travada.getIdTurma());

            String motivo = MatriculaValidador.aparar(justificativa)
                    + (aviso.isPresent() ? " | Fora da faixa etária, confirmado pelo usuário" : "");
            auditoria.registrarAlteracao(conexao, "matricula", atual.getIdMatricula(),
                    "id_turma", atual.getNomeTurma(), travada.getNome(), motivo);

            // RF-11 / RN-08: a andarilha era a exceção para a turma antiga; na troca ela termina
            if (atual.isAndarilha()) {
                matriculaDAO.atualizarAndarilha(conexao, atual.getIdMatricula(), false, null, null);
                registrarMudancasAndarilha(conexao, atual, false, null, null,
                        "Andarilha encerrada pela troca de turma");
            }
            return null;
        });
    }

    /**
     * RF-11 / FA-02: marca/desmarca a matrícula ATIVA como andarilha,
     * com motivo e prazo previsto (auditado).
     */
    public void registrarAndarilha(AlunoTurma aluno, boolean andarilha, String motivo,
                                   LocalDate andarilhaAte) throws SQLException {
        ControleAcesso.exigir(Permissao.CONFIRMAR_MATRICULA); // RNF-03
        exigirSemErros(TurmaValidador.validarAndarilha(aluno, andarilha, motivo, andarilhaAte, LocalDate.now()));
        String motivoNovo = andarilha ? MatriculaValidador.aparar(motivo) : null;
        LocalDate prazoNovo = andarilha ? andarilhaAte : null;

        Transacao.executar(conexao -> {
            Matricula atual = matriculaDAO.buscarETravar(conexao, aluno.getIdMatricula())
                    .filter(Matricula::isAtiva)
                    .orElseThrow(() -> new MatriculaException(
                            "A matrícula não está mais ATIVA. Atualize a lista."));

            matriculaDAO.atualizarAndarilha(conexao, atual.getIdMatricula(), andarilha, motivoNovo, prazoNovo);
            registrarMudancasAndarilha(conexao, atual, andarilha, motivoNovo, prazoNovo,
                    andarilha ? "Registro de andarilha" : "Andarilha removida");
            return null;
        });
    }

    /** RNF-02: grava no histórico só os campos da andarilha que mudaram. */
    private void registrarMudancasAndarilha(Connection conexao, Matricula atual, boolean andarilha,
                                            String motivoNovo, LocalDate prazoNovo,
                                            String justificativa) throws SQLException {
        int id = atual.getIdMatricula();
        if (atual.isAndarilha() != andarilha) {
            auditoria.registrarAlteracao(conexao, "matricula", id,
                    "andarilha", atual.isAndarilha(), andarilha, justificativa);
        }
        if (!Objects.equals(atual.getMotivoAndarilha(), motivoNovo)) {
            auditoria.registrarAlteracao(conexao, "matricula", id,
                    "motivo_andarilha", atual.getMotivoAndarilha(), motivoNovo, justificativa);
        }
        if (!Objects.equals(atual.getAndarilhaAte(), prazoNovo)) {
            auditoria.registrarAlteracao(conexao, "matricula", id,
                    "andarilha_ate", atual.getAndarilhaAte(), prazoNovo, justificativa);
        }
    }

    /** Lança erro com todas as mensagens quando a lista de erros não está vazia. */
    private static void exigirSemErros(List<String> erros) {
        if (!erros.isEmpty()) {
            throw new MatriculaException(String.join("\n", erros));
        }
    }
}
