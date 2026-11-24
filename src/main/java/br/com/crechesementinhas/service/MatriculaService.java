package br.com.crechesementinhas.service;

import br.com.crechesementinhas.dao.AlunoDAO;
import br.com.crechesementinhas.dao.MatriculaDAO;
import br.com.crechesementinhas.model.Aluno;
import br.com.crechesementinhas.model.Matricula;
import br.com.crechesementinhas.util.ControleAcesso;
import br.com.crechesementinhas.util.Permissao;
import br.com.crechesementinhas.util.Transacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class MatriculaService {

    // Valores de referência por faixa etária (ajuste conforme tabela oficial RN-07)
    private static final BigDecimal MENSALIDADE_BERCARIO = new BigDecimal("850.00");    // 0 a 11 meses
    private static final BigDecimal MENSALIDADE_MATERNAL_I = new BigDecimal("780.00");  // 12 a 23 meses
    private static final BigDecimal MENSALIDADE_MATERNAL_II = new BigDecimal("720.00"); // 24 a 35 meses
    private static final BigDecimal MENSALIDADE_JARDIM = new BigDecimal("680.00");      // 36 meses ou mais

    private final AlunoDAO alunoDAO;
    private final MatriculaDAO matriculaDAO;
    private final AuditoriaService auditoriaService;

    public MatriculaService() {
        this.alunoDAO = new AlunoDAO();
        this.matriculaDAO = new MatriculaDAO();
        this.auditoriaService = new AuditoriaService();
    }

    /**
     * Calcula a idade exata da criança em meses completos na data de referência.
     */
    public int calcularIdadeEmMeses(LocalDate dataNascimento, LocalDate dataReferencia) {
        if (dataNascimento == null || dataReferencia == null || dataNascimento.isAfter(dataReferencia)) {
            throw new IllegalArgumentException("EX-06: Data de nascimento inválida ou futura.");
        }
        return (int) ChronoUnit.MONTHS.between(dataNascimento, dataReferencia);
    }

    /**
     * Formata a idade para exibição na interface (ex.: "1 ano(s) e 4 mês(es)").
     */
    public String formatarIdade(LocalDate dataNascimento) {
        LocalDate hoje = LocalDate.now();
        if (dataNascimento == null || dataNascimento.isAfter(hoje)) {
            return "Data inválida";
        }
        Period periodo = Period.between(dataNascimento, hoje);
        return periodo.getYears() + " ano(s) e " + periodo.getMonths() + " mês(es)";
    }

    /**
     * RN-07: Calcula o valor da mensalidade conforme a faixa etária da criança.
     */
    public BigDecimal calcularMensalidadePorFaixaEtaria(LocalDate dataNascimento) {
        int meses = calcularIdadeEmMeses(dataNascimento, LocalDate.now());

        if (meses < 12) {
            return MENSALIDADE_BERCARIO;
        } else if (meses < 24) {
            return MENSALIDADE_MATERNAL_I;
        } else if (meses < 36) {
            return MENSALIDADE_MATERNAL_II;
        } else {
            return MENSALIDADE_JARDIM;
        }
    }

    /**
     * RF-02, RF-03, RN-09: Cadastra o aluno e efetiva a matrícula em uma única transação.
     */
    public Matricula cadastrarAlunoEMatricula(Aluno aluno, int idTurma, LocalDate dataMatricula,
                                              boolean andarilha, String motivoAndarilha) throws Exception {
        // RNF-03: Apenas perfis autorizados podem cadastrar matrícula
        ControleAcesso.exigir(Permissao.CADASTRAR_MATRICULA);

        // EX-06: Valida campos obrigatórios e regras de negócio
        validarDadosCadastro(aluno, idTurma, dataMatricula, andarilha, motivoAndarilha);

        Matricula matricula = new Matricula();
        matricula.setNumeroMatricula(gerarNumeroMatricula());
        matricula.setIdTurma(idTurma);
        matricula.setStatus("ATIVA"); // RN-09: Matrícula inicia como ATIVA
        matricula.setDataMatricula(dataMatricula);
        matricula.setAndarilha(andarilha);
        matricula.setMotivoAndarilha(andarilha ? motivoAndarilha.trim() : null);

        Transacao.executar(conexao -> {
            alunoDAO.inserir(conexao, aluno);

            matricula.setIdAluno(aluno.getIdAluno());
            matriculaDAO.inserir(conexao, matricula);

            // RNF-02: Grava auditoria na tabela historico_alteracao
            auditoriaService.registrarInsercao(conexao, "aluno", aluno.getIdAluno());
            auditoriaService.registrarInsercao(conexao, "matricula", matricula.getIdMatricula());
            return null;
        });

        return matricula;
    }

    private void validarDadosCadastro(Aluno aluno, int idTurma, LocalDate dataMatricula,
                                      boolean andarilha, String motivoAndarilha) {
        if (aluno == null || aluno.getNome() == null || aluno.getNome().isBlank()) {
            throw new IllegalArgumentException("EX-06: O nome da criança é obrigatório.");
        }
        if (aluno.getDataNascimento() == null || aluno.getDataNascimento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("EX-06: Informe uma data de nascimento válida.");
        }
        if (aluno.getSexo() == null || (!aluno.getSexo().equals("F") && !aluno.getSexo().equals("M"))) {
            throw new IllegalArgumentException("EX-06: Selecione o sexo da criança (F ou M).");
        }
        if (idTurma <= 0) {
            throw new IllegalArgumentException("EX-06: Informe o ID da turma.");
        }
        if (dataMatricula == null) {
            throw new IllegalArgumentException("EX-06: A data da matrícula é obrigatória.");
        }
        if (andarilha && (motivoAndarilha == null || motivoAndarilha.isBlank())) {
            throw new IllegalArgumentException("EX-06: Informe o motivo quando a matrícula for marcada como andarilha.");
        }
    }

    private String gerarNumeroMatricula() {
        String sufixo = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return Year.now().getValue() + "-" + sufixo;
    }
}