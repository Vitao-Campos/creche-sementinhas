package br.com.crechesementinhas.service;

import br.com.crechesementinhas.dao.ContratoDAO;
import br.com.crechesementinhas.dao.MatriculaDAO;
import br.com.crechesementinhas.dao.TurmaDAO;
import br.com.crechesementinhas.model.Aluno;
import br.com.crechesementinhas.model.Contrato;
import br.com.crechesementinhas.model.Desligamento;
import br.com.crechesementinhas.model.FaixaMensalidade;
import br.com.crechesementinhas.model.Matricula;
import br.com.crechesementinhas.model.Permissao;
import br.com.crechesementinhas.model.Turma;
import br.com.crechesementinhas.util.Formatador;
import br.com.crechesementinhas.util.Transacao;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Efetivação da matrícula (RF-03, UC-01) e desligamento com cálculo proporcional (RF-10, RN-12).
 * O cadastro dos dados da criança (RF-02) fica no AlunoService; aqui a criança já existe.
 */
public class MatriculaService {

    private final MatriculaDAO matriculaDAO = new MatriculaDAO();
    private final TurmaDAO turmaDAO = new TurmaDAO();
    private final ContratoDAO contratoDAO = new ContratoDAO();
    private final MensalidadeService mensalidadeService = new MensalidadeService();
    private final AuditoriaService auditoria = new AuditoriaService();

    public List<Turma> listarTurmas() throws SQLException {
        return turmaDAO.listar();
    }

    public Optional<Matricula> buscarAtiva(Aluno aluno) throws SQLException {
        return matriculaDAO.buscarAtivaPorAluno(aluno.getIdAluno());
    }

    public List<Matricula> historico(Aluno aluno) throws SQLException {
        return matriculaDAO.listarPorAluno(aluno.getIdAluno());
    }

    /** Aviso de faixa etária para a tela pedir confirmação antes de efetivar. */
    public Optional<String> avisoFaixaEtaria(Aluno aluno, Turma turma, LocalDate dataMatricula) {
        return MatriculaValidador.avisoFaixaEtaria(aluno, turma, dataMatricula);
    }

    /**
     * RF-03 / RN-09: efetiva a matrícula da criança na turma, já como ATIVA.
     *
     * @param confirmouForaDaFaixa o usuário confirmou o aviso de faixa etária (se houve)
     */
    public Matricula efetivar(Aluno aluno, Turma turma, LocalDate dataMatricula, boolean andarilha,
                              String motivoAndarilha, boolean confirmouForaDaFaixa) throws SQLException {
        ControleAcesso.exigir(Permissao.CADASTRAR_MATRICULA); // RNF-03

        // Primeira conferência, rápida, só com os dados da tela (EX-06)
        exigirSemErros(MatriculaValidador.validarEfetivacao(aluno, turma, dataMatricula,
                andarilha, motivoAndarilha, false, LocalDate.now()));

        Matricula matricula = new Matricula();
        matricula.setIdAluno(aluno.getIdAluno());
        matricula.setIdTurma(turma.getIdTurma());
        matricula.setStatus(Matricula.ATIVA); // RN-09: a matrícula efetivada nasce ATIVA
        matricula.setDataMatricula(dataMatricula);
        matricula.setAndarilha(andarilha);
        matricula.setMotivoAndarilha(andarilha ? MatriculaValidador.aparar(motivoAndarilha) : null);

        return Transacao.executar(conexao -> {
            // Segunda conferência, com a turma TRAVADA: vale o que está no banco agora
            Turma atual = turmaDAO.buscarETravar(conexao, turma.getIdTurma())
                    .orElseThrow(() -> new MatriculaException("Turma não encontrada."));
            boolean temAtiva = matriculaDAO.buscarAtivaPorAluno(conexao, aluno.getIdAluno()).isPresent();
            exigirSemErros(MatriculaValidador.validarEfetivacao(aluno, atual, dataMatricula,
                    andarilha, motivoAndarilha, temAtiva, LocalDate.now()));

            Optional<String> aviso = MatriculaValidador.avisoFaixaEtaria(aluno, atual, dataMatricula);
            if (aviso.isPresent() && !confirmouForaDaFaixa) {
                throw new MatriculaException(aviso.get() + " Confirme para matricular mesmo assim.");
            }

            matricula.setNumeroMatricula(matriculaDAO.proximoNumero(conexao, dataMatricula.getYear()));
            matriculaDAO.inserir(conexao, matricula);
            matricula.setNomeTurma(atual.getNome());

            // RNF-02
            auditoria.registrarInsercao(conexao, "matricula", matricula.getIdMatricula());
            if (aviso.isPresent()) {
                auditoria.registrarAlteracao(conexao, "matricula", matricula.getIdMatricula(),
                        "id_turma", null, atual.getNome(),
                        "Matrícula fora da faixa etária confirmada pelo usuário.");
            }
            if (matricula.isAndarilha()) {
                auditoria.registrarAlteracao(conexao, "matricula", matricula.getIdMatricula(),
                        "motivo_andarilha", null, matricula.getMotivoAndarilha(), "Matrícula andarilha.");
            }
            return matricula;
        });
    }

    /**
     * RF-10 / RN-12: calcula o desligamento SEM gravar (para a tela mostrar antes de confirmar).
     * A mensalidade base é a do contrato VIGENTE; sem contrato, a da tabela por faixa etária (RN-07).
     */
    public Desligamento simularDesligamento(Matricula matricula, Aluno aluno,
                                            LocalDate comunicacao, LocalDate desligamento) throws SQLException {
        if (matricula == null || desligamento == null || comunicacao == null) {
            throw new MatriculaException("Informe as datas de comunicação e de desligamento.");
        }
        BigDecimal base = null;
        String origem = "contrato vigente";
        for (Contrato c : contratoDAO.listarPorMatricula(matricula.getIdMatricula())) {
            if ("VIGENTE".equals(c.getStatus()) && c.getValorMensalidade() != null) {
                base = c.getValorMensalidade();
            }
        }
        if (base == null) {
            origem = "tabela por faixa etária";
            base = mensalidadeService.calcular(aluno.getDataNascimento(), desligamento)
                    .map(FaixaMensalidade::getValor)
                    .orElseThrow(() -> new MatriculaException(
                            "Não há faixa de mensalidade para a idade da criança. Confira a tabela de mensalidades."));
        }
        return new Desligamento(
                desligamento, base, origem,
                CalculadoraMensalidade.diasCobrados(desligamento),
                YearMonth.from(desligamento).lengthOfMonth(),
                CalculadoraMensalidade.proporcional(base, desligamento),
                CalculadoraMensalidade.diasDeAviso(comunicacao, desligamento));
    }

    /**
     * RF-10 / RN-12: desliga a criança. Encerra a matrícula e o contrato vigente,
     * tudo com auditoria (RNF-02). Só Direção e Suporte (RNF-03).
     *
     * @param confirmouAvisoCurto o usuário confirmou desligar com menos de 30 dias de aviso
     */
    public Desligamento desligar(Matricula matricula, Aluno aluno, LocalDate comunicacao,
                                 LocalDate desligamento, String justificativa,
                                 boolean confirmouAvisoCurto) throws SQLException {
        ControleAcesso.exigir(Permissao.DESLIGAR_ALUNO);
        exigirSemErros(MatriculaValidador.validarDesligamento(matricula, comunicacao, desligamento, justificativa));

        Desligamento calculo = simularDesligamento(matricula, aluno, comunicacao, desligamento);
        if (calculo.diasDeAviso() < CalculadoraMensalidade.AVISO_PREVIO_DIAS && !confirmouAvisoCurto) {
            throw new MatriculaException("O aviso foi de " + calculo.diasDeAviso() + " dias (o contrato pede "
                    + CalculadoraMensalidade.AVISO_PREVIO_DIAS + "). Confirme para desligar mesmo assim.");
        }

        String motivo = MatriculaValidador.aparar(justificativa)
                + " | Último mês: " + Formatador.dinheiro(calculo.valorProporcional())
                + " (" + calculo.diasCobrados() + "/" + calculo.diasNoMes() + " dias)";

        Transacao.executar(conexao -> {
            matriculaDAO.encerrar(conexao, matricula.getIdMatricula(), desligamento);
            auditoria.registrarAlteracao(conexao, "matricula", matricula.getIdMatricula(),
                    "status", Matricula.ATIVA, Matricula.ENCERRADA, motivo);
            auditoria.registrarAlteracao(conexao, "matricula", matricula.getIdMatricula(),
                    "data_encerramento", null, desligamento, motivo);

            Integer idContrato = contratoDAO.encerrarVigentePorDesligamento(
                    conexao, matricula.getIdMatricula(), desligamento);
            if (idContrato != null) {
                auditoria.registrarAlteracao(conexao, "contrato", idContrato,
                        "status", "VIGENTE", "ENCERRADO", motivo);
            }
            return null;
        });

        matricula.setStatus(Matricula.ENCERRADA);
        matricula.setDataEncerramento(desligamento);
        return calculo;
    }

    private static void exigirSemErros(List<String> erros) {
        if (!erros.isEmpty()) {
            throw new MatriculaException(String.join("\n", erros));
        }
    }
}
