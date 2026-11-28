package br.com.crechesementinhas.service;

import br.com.crechesementinhas.dao.HistoricoAlteracaoDAO;
import br.com.crechesementinhas.model.HistoricoAlteracao;
import br.com.crechesementinhas.model.TipoAlteracao;
import br.com.crechesementinhas.util.Sessao;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Trilha de auditoria do sistema (RNF-02).
 * Grava automaticamente o usuário logado e a data/hora.
 *
 * Convenção para "entidade": nome da tabela, em minúsculas
 * (ex.: "aluno", "matricula", "responsavel", "ficha_anamnese").
 *
 * Sempre chamar DENTRO de Transacao.executar(...), passando a mesma conexão
 * usada na alteração: assim alteração e auditoria acontecem juntas ou não acontecem.
 */
public class AuditoriaService {

    private final HistoricoAlteracaoDAO historicoDAO;

    /** Cria o serviço de auditoria com o acesso ao banco padrão. */
    public AuditoriaService() {
        this(new HistoricoAlteracaoDAO());
    }

    /** Cria o serviço de auditoria com o acesso ao histórico informado (usado em testes). */
    public AuditoriaService(HistoricoAlteracaoDAO historicoDAO) {
        this.historicoDAO = historicoDAO;
    }

    /** Registra que um novo registro foi criado. */
    public void registrarInsercao(Connection conexao, String entidade, int idEntidade)
            throws SQLException {
        gravar(conexao, entidade, idEntidade, TipoAlteracao.INSERCAO, null, null, null, null);
    }

    /**
     * Registra a alteração de UM campo.
     * Se o valor não mudou, não grava nada (evita poluir o histórico).
     */
    public void registrarAlteracao(Connection conexao, String entidade, int idEntidade,
                                   String campo, Object valorAnterior, Object valorNovo,
                                   String justificativa) throws SQLException {

        if (Objects.equals(valorAnterior, valorNovo)) {
            return;
        }
        gravar(conexao, entidade, idEntidade, TipoAlteracao.ALTERACAO,
               campo, emTexto(valorAnterior), emTexto(valorNovo), justificativa);
    }

    /** Registra que um registro foi excluído. */
    public void registrarExclusao(Connection conexao, String entidade, int idEntidade,
                                  String justificativa) throws SQLException {
        gravar(conexao, entidade, idEntidade, TipoAlteracao.EXCLUSAO, null, null, null, justificativa);
    }

    /** Histórico de um registro, do mais recente para o mais antigo. */
    public List<HistoricoAlteracao> consultarHistorico(String entidade, int idEntidade)
            throws SQLException {
        return historicoDAO.listarPorEntidade(entidade, idEntidade);
    }

    /** Monta o registro de histórico com o usuário logado e o grava na mesma transação. */
    private void gravar(Connection conexao, String entidade, int idEntidade, TipoAlteracao tipo,
                        String campo, String anterior, String novo, String justificativa)
            throws SQLException {

        // O autor vem da sessão: o colega da frente não precisa (nem consegue) informar.
        int idUsuario = Sessao.getUsuarioLogado().getIdUsuario();

        HistoricoAlteracao registro = new HistoricoAlteracao(
                null, idUsuario, LocalDateTime.now(), entidade, idEntidade,
                tipo, campo, anterior, novo, justificativa);

        historicoDAO.inserir(conexao, registro);
    }

    // Object → String: funciona com textos, números, datas e booleanos.
    /** Converte o valor para texto (nulo continua nulo). */
    private String emTexto(Object valor) {
        return valor == null ? null : valor.toString();
    }
}