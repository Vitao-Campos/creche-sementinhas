package br.com.crechesementinhas.service;

import br.com.crechesementinhas.dao.AlunoDAO;
import br.com.crechesementinhas.model.Aluno;
import br.com.crechesementinhas.model.Permissao;
import br.com.crechesementinhas.util.Transacao;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Regras do cadastro de alunos (RF-02, RNF-02, RNF-03; CPF único conforme a seção 6.2).
 * Toda gravação de aluno e o seu registro de auditoria acontecem na mesma transação.
 */
public class AlunoService {

    private static final String ENTIDADE = "aluno";
    private static final String SQLSTATE_VIOLACAO_UNICA = "23505";

    private final AlunoDAO alunoDAO;
    private final AuditoriaService auditoria;

    public AlunoService() {
        this(new AlunoDAO(), new AuditoriaService());
    }

    public AlunoService(AlunoDAO alunoDAO, AuditoriaService auditoria) {
        this.alunoDAO = alunoDAO;
        this.auditoria = auditoria;
    }

    /**
     * Lista os alunos em ordem alfabética, filtrando por parte do nome.
     *
     * @param busca parte do nome; vazio ou null lista todos
     */
    public List<Aluno> listar(String busca) throws SQLException {
        return alunoDAO.listar(busca);
    }

    /**
     * Cadastra um aluno novo.
     *
     * @param aluno os dados digitados (o objeto é limpo e recebe o id gerado)
     * @return o próprio aluno, já com o id
     * @throws CadastroAlunoException se algum dado for inválido ou o CPF já existir (UNIQUE da seção 6.2)
     * @throws SecurityException      se o perfil do usuário não puder cadastrar
     */
    public Aluno cadastrar(Aluno aluno) throws SQLException {
        ControleAcesso.exigir(Permissao.CADASTRAR_MATRICULA);
        prepararEValidar(aluno);

        try {
            Transacao.executar(conexao -> {
                conferirCpfUnico(conexao, aluno);
                int id = alunoDAO.inserir(conexao, aluno);
                aluno.setIdAluno(id);
                auditoria.registrarInsercao(conexao, ENTIDADE, id);
                return null;
            });
            return aluno;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    /**
     * Altera os dados de um aluno e registra na auditoria cada campo que mudou.
     *
     * @param aluno         os dados digitados, com o idAluno preenchido
     * @param justificativa motivo da alteração (opcional, vai para o histórico)
     * @throws CadastroAlunoException se algum dado for inválido, o aluno não existir ou o CPF for de outro aluno
     * @throws SecurityException      se o perfil do usuário não puder alterar
     */
    public Aluno atualizar(Aluno aluno, String justificativa) throws SQLException {
        ControleAcesso.exigir(Permissao.CADASTRAR_MATRICULA);
        if (aluno.getIdAluno() == null) {
            throw new CadastroAlunoException("Escolha na lista o aluno que será alterado.");
        }
        prepararEValidar(aluno);
        String motivo = justificativa == null || justificativa.isBlank() ? null : justificativa.trim();

        try {
            Transacao.executar(conexao -> {
                Aluno anterior = alunoDAO.buscarPorId(conexao, aluno.getIdAluno())
                        .orElseThrow(() -> new CadastroAlunoException("Aluno não encontrado."));
                conferirCpfUnico(conexao, aluno);

                alunoDAO.atualizar(conexao, aluno);
                registrarMudancas(conexao, anterior, aluno, motivo);
                return null;
            });
            return aluno;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    private void prepararEValidar(Aluno aluno) {
        AlunoValidador.normalizar(aluno);
        List<String> erros = AlunoValidador.validar(aluno, LocalDate.now());
        if (!erros.isEmpty()) {
            throw new CadastroAlunoException(String.join("\n", erros));
        }
    }

    /** CPF único (seção 6.2): o CPF da criança (quando houver) não se repete. */
    private void conferirCpfUnico(Connection conexao, Aluno aluno) throws SQLException {
        if (aluno.getCpf() != null
                && alunoDAO.existeCpf(conexao, aluno.getCpf(), aluno.getIdAluno())) {
            throw new CadastroAlunoException("Já existe um aluno cadastrado com este CPF.");
        }
    }

    /** Um registro de auditoria por campo alterado (campo igual não grava nada). */
    private void registrarMudancas(Connection conexao, Aluno antes, Aluno depois, String motivo)
            throws SQLException {
        int id = depois.getIdAluno();
        auditoria.registrarAlteracao(conexao, ENTIDADE, id, "nome",
                antes.getNome(), depois.getNome(), motivo);
        auditoria.registrarAlteracao(conexao, ENTIDADE, id, "data_nascimento",
                antes.getDataNascimento(), depois.getDataNascimento(), motivo);
        auditoria.registrarAlteracao(conexao, ENTIDADE, id, "cpf",
                antes.getCpf(), depois.getCpf(), motivo);
        auditoria.registrarAlteracao(conexao, ENTIDADE, id, "sexo",
                antes.getSexo().name(), depois.getSexo().name(), motivo);
        auditoria.registrarAlteracao(conexao, ENTIDADE, id, "naturalidade",
                antes.getNaturalidade(), depois.getNaturalidade(), motivo);
        auditoria.registrarAlteracao(conexao, ENTIDADE, id, "nacionalidade",
                antes.getNacionalidade(), depois.getNacionalidade(), motivo);
        auditoria.registrarAlteracao(conexao, ENTIDADE, id, "observacao",
                antes.getObservacao(), depois.getObservacao(), motivo);
    }

    /**
     * Duas pessoas gravando o mesmo CPF ao mesmo tempo passam pela conferência prévia,
     * mas o UNIQUE do banco barra a segunda. Aqui vira uma mensagem amigável.
     */
    private SQLException traduzir(SQLException e) {
        if (SQLSTATE_VIOLACAO_UNICA.equals(e.getSQLState())) {
            throw new CadastroAlunoException("Já existe um aluno cadastrado com este CPF.", e);
        }
        return e;
    }
}
