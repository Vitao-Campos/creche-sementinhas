package br.com.crechesementinhas.service;

import br.com.crechesementinhas.dao.AlunoPessoaAutorizadaDAO;
import br.com.crechesementinhas.dao.PessoaAutorizadaDAO;
import br.com.crechesementinhas.model.AlunoPessoaAutorizada;
import br.com.crechesementinhas.model.PessoaAutorizada;
import br.com.crechesementinhas.model.Permissao;
import br.com.crechesementinhas.util.Transacao;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Regras de quem pode buscar a criança (RF-02, RN-04, RNF-02, RNF-03).
 * Gravação e auditoria acontecem na mesma transação.
 */
public class AutorizadoService {

    private static final String ENTIDADE_PESSOA = "pessoa_autorizada";
    private static final String ENTIDADE_AUTORIZACAO = "aluno_pessoa_autorizada";
    private static final String SQLSTATE_VIOLACAO_UNICA = "23505";
    private static final String MSG_JA_AUTORIZADA = "Esta pessoa já está autorizada para esta criança.";

    private final PessoaAutorizadaDAO pessoaDAO;
    private final AlunoPessoaAutorizadaDAO autorizacaoDAO;
    private final AuditoriaService auditoria;

    public AutorizadoService() {
        this(new PessoaAutorizadaDAO(), new AlunoPessoaAutorizadaDAO(), new AuditoriaService());
    }

    public AutorizadoService(PessoaAutorizadaDAO pessoaDAO, AlunoPessoaAutorizadaDAO autorizacaoDAO,
                             AuditoriaService auditoria) {
        this.pessoaDAO = pessoaDAO;
        this.autorizacaoDAO = autorizacaoDAO;
        this.auditoria = auditoria;
    }

    /** RN-04: quem pode buscar a criança (ativos primeiro); equivale à consulta Q02 do documento. */
    public List<AlunoPessoaAutorizada> listarPorAluno(int idAluno) throws SQLException {
        return autorizacaoDAO.listarPorAluno(idAluno);
    }

    /**
     * Autoriza uma pessoa a buscar a criança. Se a mesma pessoa (mesmo RG e nome) já foi
     * cadastrada para outra criança, ela é reaproveitada em vez de duplicada.
     *
     * @param dados idAluno, nome, RG, telefone, parentesco (opcional) e tipo de autorização
     * @throws CadastroAutorizadoException dados inválidos ou pessoa já autorizada para a criança
     */
    public AlunoPessoaAutorizada autorizar(AlunoPessoaAutorizada dados) throws SQLException {
        ControleAcesso.exigir(Permissao.CADASTRAR_MATRICULA);
        AutorizadoValidador.normalizar(dados);
        List<String> erros = AutorizadoValidador.validar(dados);
        if (!erros.isEmpty()) {
            throw new CadastroAutorizadoException(String.join("\n", erros));
        }

        try {
            Transacao.executar(conexao -> {
                Optional<PessoaAutorizada> existente =
                        pessoaDAO.buscarPorRgENome(conexao, dados.getRg(), dados.getNome());

                int idPessoa;
                if (existente.isPresent()) {
                    idPessoa = existente.get().getIdPessoaAutorizada();
                } else {
                    PessoaAutorizada nova = new PessoaAutorizada();
                    nova.setNome(dados.getNome());
                    nova.setRg(dados.getRg());
                    nova.setTelefone(dados.getTelefone());
                    idPessoa = pessoaDAO.inserir(conexao, nova);
                    auditoria.registrarInsercao(conexao, ENTIDADE_PESSOA, idPessoa);
                }
                dados.setIdPessoaAutorizada(idPessoa);

                if (autorizacaoDAO.existeAtiva(conexao, dados.getIdAluno(), idPessoa)) {
                    throw new CadastroAutorizadoException(MSG_JA_AUTORIZADA);
                }
                int id = autorizacaoDAO.inserir(conexao, dados);
                dados.setIdAlunoPessoaAutorizada(id);
                dados.setAtivo(true);
                auditoria.registrarInsercao(conexao, ENTIDADE_AUTORIZACAO, id);
                return null;
            });
            return dados;
        } catch (SQLException e) {
            // Dois cadastros simultâneos: o índice único do banco barra o segundo.
            if (SQLSTATE_VIOLACAO_UNICA.equals(e.getSQLState())) {
                throw new CadastroAutorizadoException(MSG_JA_AUTORIZADA, e);
            }
            throw e;
        }
    }

    /**
     * Retira a autorização (a pessoa deixa de poder buscar a criança). Nada é apagado:
     * o histórico de quem já foi autorizado fica guardado.
     *
     * @param justificativa motivo (opcional, vai para o histórico)
     */
    public void desativar(int idAutorizacao, String justificativa) throws SQLException {
        ControleAcesso.exigir(Permissao.CADASTRAR_MATRICULA);
        String motivo = justificativa == null || justificativa.isBlank() ? null : justificativa.trim();

        Transacao.executar(conexao -> {
            AlunoPessoaAutorizada atual = autorizacaoDAO.buscarPorId(conexao, idAutorizacao)
                    .orElseThrow(() -> new CadastroAutorizadoException("Autorização não encontrada."));
            if (!atual.isAtivo()) {
                throw new CadastroAutorizadoException("Esta autorização já foi retirada.");
            }
            autorizacaoDAO.atualizarAtivo(conexao, idAutorizacao, false);
            auditoria.registrarAlteracao(conexao, ENTIDADE_AUTORIZACAO, idAutorizacao,
                    "ativo", true, false, motivo);
            return null;
        });
    }
}
