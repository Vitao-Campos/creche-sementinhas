package br.com.crechesementinhas.dao;

import br.com.crechesementinhas.model.HistoricoAlteracao;
import br.com.crechesementinhas.model.TipoAlteracao;
import br.com.crechesementinhas.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso à tabela historico_alteracao (RNF-02).
 */
public class HistoricoAlteracaoDAO {

    private static final String SQL_INSERIR = """
            INSERT INTO historico_alteracao
                   (id_usuario, data_hora, entidade, id_entidade, tipo_alteracao,
                    campo_alterado, valor_anterior, valor_novo, justificativa)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_LISTAR_POR_ENTIDADE = """
            SELECT id_historico, id_usuario, data_hora, entidade, id_entidade,
                   tipo_alteracao, campo_alterado, valor_anterior, valor_novo, justificativa
              FROM historico_alteracao
             WHERE entidade = ? AND id_entidade = ?
             ORDER BY data_hora DESC
            """;

    /**
     * Grava um registro usando a conexão RECEBIDA (de uma transação aberta).
     * Não fecha a conexão: quem abriu a transação é quem fecha.
     */
    public void inserir(Connection conexao, HistoricoAlteracao registro) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR)) {
            comando.setInt(1, registro.idUsuario());
            comando.setObject(2, registro.dataHora());
            comando.setString(3, registro.entidade());
            comando.setInt(4, registro.idEntidade());
            comando.setString(5, registro.tipoAlteracao().name());
            comando.setString(6, registro.campoAlterado());
            comando.setString(7, registro.valorAnterior());
            comando.setString(8, registro.valorNovo());
            comando.setString(9, registro.justificativa());
            comando.executeUpdate();
        }
    }

    /**
     * Histórico de um registro, do mais recente para o mais antigo.
     * Ex.: listarPorEntidade("Aluno", 5) mostra tudo o que mudou no aluno 5 (RN-08, RN-10).
     */
    public List<HistoricoAlteracao> listarPorEntidade(String entidade, int idEntidade)
            throws SQLException {

        List<HistoricoAlteracao> historico = new ArrayList<>();

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(SQL_LISTAR_POR_ENTIDADE)) {

            comando.setString(1, entidade);
            comando.setInt(2, idEntidade);

            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    historico.add(mapear(resultado));
                }
            }
        }
        return historico;
    }

    private HistoricoAlteracao mapear(ResultSet resultado) throws SQLException {
        return new HistoricoAlteracao(
                resultado.getInt("id_historico"),
                resultado.getInt("id_usuario"),
                resultado.getObject("data_hora", LocalDateTime.class),
                resultado.getString("entidade"),
                resultado.getInt("id_entidade"),
                TipoAlteracao.valueOf(resultado.getString("tipo_alteracao")),
                resultado.getString("campo_alterado"),
                resultado.getString("valor_anterior"),
                resultado.getString("valor_novo"),
                resultado.getString("justificativa"));
    }
}