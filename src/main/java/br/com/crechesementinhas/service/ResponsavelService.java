package br.com.crechesementinhas.service;

import br.com.crechesementinhas.dao.AlunoResponsavelDAO;
import br.com.crechesementinhas.dao.MatriculaDAO;
import br.com.crechesementinhas.dao.ResponsavelDAO;
import br.com.crechesementinhas.model.AlunoResponsavel;
import br.com.crechesementinhas.model.Permissao;
import br.com.crechesementinhas.model.Responsavel;
import br.com.crechesementinhas.util.Cpf;
import br.com.crechesementinhas.util.Transacao;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Regras do cadastro de responsáveis e dos vínculos com as crianças
 * (RF-02, RN-01 a RN-03, RNF-02, RNF-03; CPF único conforme a seção 6.2).
 * Toda gravação e o seu registro de auditoria acontecem na mesma transação.
 */
public class ResponsavelService {

    private static final String ENTIDADE = "responsavel";
    private static final String ENTIDADE_VINCULO = "aluno_responsavel";
    private static final String SQLSTATE_VIOLACAO_UNICA = "23505";
    private static final String MSG_CPF_REPETIDO = "Já existe um responsável cadastrado com este CPF.";

    private final ResponsavelDAO responsavelDAO;
    private final AlunoResponsavelDAO vinculoDAO;
    private final AuditoriaService auditoria;
    private final MatriculaDAO matriculaDAO = new MatriculaDAO();

    /** Cria o serviço com os acessos ao banco padrão. */
    public ResponsavelService() {
        this(new ResponsavelDAO(), new AlunoResponsavelDAO(), new AuditoriaService());
    }

    /** Cria o serviço com os acessos informados (usado em testes). */
    public ResponsavelService(ResponsavelDAO responsavelDAO, AlunoResponsavelDAO vinculoDAO,
                              AuditoriaService auditoria) {
        this.responsavelDAO = responsavelDAO;
        this.vinculoDAO = vinculoDAO;
        this.auditoria = auditoria;
    }

    /** Lista em ordem alfabética, filtrando por parte do nome ou do CPF. */
    public List<Responsavel> listar(String busca) throws SQLException {
        return responsavelDAO.listar(busca);
    }

    /** Cadastra um responsável novo (CPF obrigatório, válido e único). */
    public Responsavel cadastrar(Responsavel responsavel) throws SQLException {
        ControleAcesso.exigir(Permissao.CADASTRAR_MATRICULA);
        prepararEValidar(responsavel);

        try {
            Transacao.executar(conexao -> {
                conferirCpfUnico(conexao, responsavel);
                int id = responsavelDAO.inserir(conexao, responsavel);
                responsavel.setIdResponsavel(id);
                auditoria.registrarInsercao(conexao, ENTIDADE, id);
                return null;
            });
            return responsavel;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    /**
     * Altera os dados do responsável; cada campo que mudou vira um registro de auditoria (RNF-02).
     *
     * @param justificativa motivo da alteração (opcional)
     */
    public Responsavel atualizar(Responsavel responsavel, String justificativa) throws SQLException {
        ControleAcesso.exigir(Permissao.CADASTRAR_MATRICULA);
        if (responsavel.getIdResponsavel() == null) {
            throw new CadastroResponsavelException("Escolha na lista o responsável que será alterado.");
        }
        prepararEValidar(responsavel);
        String motivo = textoOuNulo(justificativa);

        try {
            Transacao.executar(conexao -> {
                Responsavel anterior = responsavelDAO.buscarPorId(conexao, responsavel.getIdResponsavel())
                        .orElseThrow(() -> new CadastroResponsavelException("Responsável não encontrado."));
                conferirCpfUnico(conexao, responsavel);

                responsavelDAO.atualizar(conexao, responsavel);
                registrarMudancas(conexao, anterior, responsavel, motivo);
                return null;
            });
            return responsavel;
        } catch (SQLException e) {
            throw traduzir(e);
        }
    }

    // ------------------------------------------------------------ vínculos

    /** Vínculos do responsável com as crianças (vigentes primeiro). */
    public List<AlunoResponsavel> listarVinculos(int idResponsavel) throws SQLException {
        return vinculoDAO.listarPorResponsavel(idResponsavel);
    }

    /** Responsáveis da criança (ficha do aluno): legais primeiro, vigentes antes dos encerrados. */
    public List<AlunoResponsavel> listarVinculosDoAluno(int idAluno) throws SQLException {
        return vinculoDAO.listarPorAluno(idAluno);
    }

    /**
     * Vincula o responsável a uma criança (parentesco, responsável legal, responsável financeiro, datas).
     *
     * @throws CadastroResponsavelException dados inválidos ou vínculo vigente repetido
     */
    public AlunoResponsavel vincular(AlunoResponsavel vinculo) throws SQLException {
        ControleAcesso.exigir(Permissao.CADASTRAR_MATRICULA);
        if (vinculo.getIdResponsavel() == null) {
            throw new CadastroResponsavelException("Escolha na lista o responsável que será vinculado.");
        }
        ResponsavelValidador.normalizarVinculo(vinculo);
        List<String> erros = ResponsavelValidador.validarVinculo(vinculo);
        if (!erros.isEmpty()) {
            throw new CadastroResponsavelException(String.join("\n", erros));
        }

        Transacao.executar(conexao -> {
            // Mesma pessoa duas vezes na mesma criança só atrapalha a consulta de quem pode retirar.
            if (vinculoDAO.existeVinculoAberto(conexao, vinculo.getIdAluno(),
                    vinculo.getIdResponsavel())) {
                throw new CadastroResponsavelException(
                        "Este responsável já está vinculado a esta criança. "
                        + "Encerre o vínculo atual antes de criar outro.");
            }
            // Um só pagador por criança: o contrato aponta para UM responsável financeiro.
            if (vinculo.isResponsavelFinanceiro()
                    && vinculoDAO.contarFinanceirosVigentes(conexao, vinculo.getIdAluno(), LocalDate.now()) > 0) {
                throw new CadastroResponsavelException(
                        "Esta criança já tem um responsável financeiro vigente. "
                        + "Encerre o vínculo dele antes de indicar outro.");
            }
            int id = vinculoDAO.inserir(conexao, vinculo);
            vinculo.setIdAlunoResponsavel(id);
            auditoria.registrarInsercao(conexao, ENTIDADE_VINCULO, id);
            return null;
        });
        return vinculo;
    }

    /**
     * Encerra um vínculo, gravando a data de fim (o histórico é mantido, nada é apagado).
     *
     * @param justificativa motivo (opcional, vai para o histórico)
     */
    public void encerrarVinculo(int idVinculo, LocalDate dataFim, String justificativa) throws SQLException {
        ControleAcesso.exigir(Permissao.CADASTRAR_MATRICULA);
        if (dataFim == null) {
            throw new CadastroResponsavelException("Informe a data de fim do vínculo.");
        }
        String motivo = textoOuNulo(justificativa);

        Transacao.executar(conexao -> {
            AlunoResponsavel vinculo = vinculoDAO.buscarPorId(conexao, idVinculo)
                    .orElseThrow(() -> new CadastroResponsavelException("Vínculo não encontrado."));
            if (vinculo.getDataFim() != null) {
                throw new CadastroResponsavelException("Este vínculo já foi encerrado.");
            }
            if (dataFim.isBefore(vinculo.getDataInicio())) {
                throw new CadastroResponsavelException(
                        "A data de fim não pode ser anterior à data de início.");
            }
            vinculoDAO.atualizarDataFim(conexao, idVinculo, dataFim);
            auditoria.registrarAlteracao(conexao, ENTIDADE_VINCULO, idVinculo,
                    "data_fim", null, dataFim, motivo);
            return null;
        });
    }

    /**
     * Corrige os papéis de um vínculo aberto (responsável legal / financeiro), com auditoria.
     * RN-01: não tira "legal" do único responsável legal de criança com matrícula ativa.
     * Um só responsável financeiro vigente por criança.
     */
    public void alterarPapeis(int idVinculo, boolean legal, boolean financeiro, String justificativa)
            throws SQLException {
        ControleAcesso.exigir(Permissao.CADASTRAR_MATRICULA);
        String motivo = textoOuNulo(justificativa);

        Transacao.executar(conexao -> {
            AlunoResponsavel vinculo = vinculoDAO.buscarPorId(conexao, idVinculo)
                    .orElseThrow(() -> new CadastroResponsavelException("Vínculo não encontrado."));
            if (vinculo.getDataFim() != null) {
                throw new CadastroResponsavelException(
                        "Este vínculo já foi encerrado e não pode ser alterado. Crie um novo vínculo.");
            }
            LocalDate hoje = LocalDate.now();
            if (financeiro && !vinculo.isResponsavelFinanceiro()
                    && vinculoDAO.contarFinanceirosVigentes(conexao, vinculo.getIdAluno(), hoje) > 0) {
                throw new CadastroResponsavelException(
                        "Esta criança já tem um responsável financeiro vigente. "
                        + "Tire a marcação dele antes de indicar outro.");
            }
            if (!legal && vinculo.isResponsavelLegal()
                    && vinculoDAO.contarLegaisVigentes(conexao, vinculo.getIdAluno(), hoje) <= 1
                    && matriculaDAO.buscarAtivaPorAluno(conexao, vinculo.getIdAluno()).isPresent()) {
                throw new CadastroResponsavelException(
                        "Este é o único responsável legal de uma criança matriculada. "
                        + "Vincule outro responsável legal antes.");
            }
            vinculoDAO.atualizarPapeis(conexao, idVinculo, legal, financeiro);
            auditoria.registrarAlteracao(conexao, ENTIDADE_VINCULO, idVinculo, "responsavel_legal",
                    vinculo.isResponsavelLegal(), legal, motivo);
            auditoria.registrarAlteracao(conexao, ENTIDADE_VINCULO, idVinculo, "responsavel_financeiro",
                    vinculo.isResponsavelFinanceiro(), financeiro, motivo);
            return null;
        });
    }

    /**
     * RN-01: a criança tem pelo menos um responsável legal vigente hoje?
     * A tela usa para avisar; a matrícula (frente B) deve usar para bloquear a conclusão.
     */
    public boolean alunoTemResponsavelLegal(int idAluno) throws SQLException {
        return Transacao.executar(conexao ->
                vinculoDAO.contarLegaisVigentes(conexao, idAluno, LocalDate.now()) > 0);
    }

    /** A criança tem alguém indicado como responsável financeiro vigente hoje? */
    public boolean alunoTemResponsavelFinanceiro(int idAluno) throws SQLException {
        return Transacao.executar(conexao ->
                vinculoDAO.contarFinanceirosVigentes(conexao, idAluno, LocalDate.now()) > 0);
    }

    // ------------------------------------------------------------ internos

    /**
     * Limpa os dados do responsável e lança erro com todas as mensagens se algo estiver inválido.
     */
    private void prepararEValidar(Responsavel responsavel) {
        ResponsavelValidador.normalizar(responsavel);
        List<String> erros = ResponsavelValidador.validar(responsavel);
        if (!erros.isEmpty()) {
            throw new CadastroResponsavelException(String.join("\n", erros));
        }
    }

    /** Consulta rápida para a tela avisar de CPF repetido antes de salvar (a trava de verdade continua no cadastro). */
    public boolean cpfJaCadastrado(String cpf, Integer idIgnorado) throws SQLException {
        String digitos = Cpf.normalizar(cpf);
        if (digitos == null || !Cpf.valido(digitos)) {
            return false;
        }
        return Transacao.executar(conexao -> responsavelDAO.existeCpf(conexao, digitos, idIgnorado));
    }

    /** CPF único (seção 6.2): o CPF do responsável não se repete. */
    private void conferirCpfUnico(Connection conexao, Responsavel responsavel) throws SQLException {
        if (responsavelDAO.existeCpf(conexao, responsavel.getCpf(), responsavel.getIdResponsavel())) {
            throw new CadastroResponsavelException(MSG_CPF_REPETIDO);
        }
    }

    /** Registra no histórico cada campo do responsável que mudou (campo igual não grava nada). */
    private void registrarMudancas(Connection conexao, Responsavel antes, Responsavel depois, String motivo)
            throws SQLException {
        int id = depois.getIdResponsavel();
        auditoria.registrarAlteracao(conexao, ENTIDADE, id, "nome", antes.getNome(), depois.getNome(), motivo);
        auditoria.registrarAlteracao(conexao, ENTIDADE, id, "cpf", antes.getCpf(), depois.getCpf(), motivo);
        auditoria.registrarAlteracao(conexao, ENTIDADE, id, "telefone", antes.getTelefone(), depois.getTelefone(), motivo);
        auditoria.registrarAlteracao(conexao, ENTIDADE, id, "email", antes.getEmail(), depois.getEmail(), motivo);
        auditoria.registrarAlteracao(conexao, ENTIDADE, id, "cep", antes.getCep(), depois.getCep(), motivo);
        auditoria.registrarAlteracao(conexao, ENTIDADE, id, "endereco", antes.getEndereco(), depois.getEndereco(), motivo);
        auditoria.registrarAlteracao(conexao, ENTIDADE, id, "profissao", antes.getProfissao(), depois.getProfissao(), motivo);
    }

    /** Duas gravações simultâneas do mesmo CPF: o UNIQUE do banco barra a segunda. */
    private SQLException traduzir(SQLException e) {
        if (SQLSTATE_VIOLACAO_UNICA.equals(e.getSQLState())) {
            throw new CadastroResponsavelException(MSG_CPF_REPETIDO, e);
        }
        return e;
    }

    /** Remove os espaços das pontas; devolve nulo se o texto ficar vazio. */
    private String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
