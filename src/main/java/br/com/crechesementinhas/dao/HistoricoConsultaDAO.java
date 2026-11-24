package br.com.crechesementinhas.dao;

import br.com.crechesementinhas.model.RegistroHistorico;
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
 * Consultas de leitura do histórico de alterações para as telas (RNF-02):
 * junta o registro com o nome do usuário e com o nome da pessoa afetada.
 * A gravação continua no HistoricoAlteracaoDAO (compartilhado).
 */
public class HistoricoConsultaDAO {

    // "referencia": quem foi afetado pelo registro, para a tela não mostrar só números.
    private static final String SQL_BASE = """
            SELECT h.data_hora, u.nome AS usuario, h.entidade, h.id_entidade,
                   CASE h.entidade
                     WHEN 'aluno' THEN
                       (SELECT a.nome FROM aluno a WHERE a.id_aluno = h.id_entidade)
                     WHEN 'responsavel' THEN
                       (SELECT r.nome FROM responsavel r WHERE r.id_responsavel = h.id_entidade)
                     WHEN 'aluno_responsavel' THEN
                       (SELECT r.nome || ' (criança: ' || a.nome || ')' FROM aluno_responsavel ar
                          JOIN responsavel r ON r.id_responsavel = ar.id_responsavel
                          JOIN aluno a ON a.id_aluno = ar.id_aluno
                         WHERE ar.id_aluno_responsavel = h.id_entidade)
                     WHEN 'pessoa_autorizada' THEN
                       (SELECT p.nome FROM pessoa_autorizada p WHERE p.id_pessoa_autorizada = h.id_entidade)
                     WHEN 'aluno_pessoa_autorizada' THEN
                       (SELECT p.nome || ' (criança: ' || a.nome || ')' FROM aluno_pessoa_autorizada ap
                          JOIN pessoa_autorizada p ON p.id_pessoa_autorizada = ap.id_pessoa_autorizada
                          JOIN aluno a ON a.id_aluno = ap.id_aluno
                         WHERE ap.id_aluno_pessoa_autorizada = h.id_entidade)
                   END AS referencia,
                   h.tipo_alteracao, h.campo_alterado, h.valor_anterior, h.valor_novo, h.justificativa
              FROM historico_alteracao h
              JOIN usuario u ON u.id_usuario = h.id_usuario
            """;

    private static final String ORDEM = """
             ORDER BY h.data_hora DESC, h.id_historico DESC
             LIMIT 1000
            """;

    // Ficha da criança: os dados dela + tudo que mudou nos vínculos, responsáveis e autorizados dela.
    private static final String SQL_POR_ALUNO = SQL_BASE + """
             WHERE (h.entidade = 'aluno' AND h.id_entidade = ?)
                OR (h.entidade = 'aluno_responsavel' AND h.id_entidade IN
                      (SELECT id_aluno_responsavel FROM aluno_responsavel WHERE id_aluno = ?))
                OR (h.entidade = 'responsavel' AND h.id_entidade IN
                      (SELECT id_responsavel FROM aluno_responsavel WHERE id_aluno = ?))
                OR (h.entidade = 'aluno_pessoa_autorizada' AND h.id_entidade IN
                      (SELECT id_aluno_pessoa_autorizada FROM aluno_pessoa_autorizada WHERE id_aluno = ?))
                OR (h.entidade = 'pessoa_autorizada' AND h.id_entidade IN
                      (SELECT id_pessoa_autorizada FROM aluno_pessoa_autorizada WHERE id_aluno = ?))
            """ + ORDEM;

    // Responsável: os dados dele + os vínculos dele com as crianças.
    private static final String SQL_POR_RESPONSAVEL = SQL_BASE + """
             WHERE (h.entidade = 'responsavel' AND h.id_entidade = ?)
                OR (h.entidade = 'aluno_responsavel' AND h.id_entidade IN
                      (SELECT id_aluno_responsavel FROM aluno_responsavel WHERE id_responsavel = ?))
            """ + ORDEM;

    /** Histórico da ficha da criança (mais recente primeiro, no máximo 1000 linhas). */
    public List<RegistroHistorico> listarPorAluno(int idAluno) throws SQLException {
        return listar(SQL_POR_ALUNO, 5, idAluno);
    }

    /** Histórico do responsável e dos vínculos dele (mais recente primeiro). */
    public List<RegistroHistorico> listarPorResponsavel(int idResponsavel) throws SQLException {
        return listar(SQL_POR_RESPONSAVEL, 2, idResponsavel);
    }

    private List<RegistroHistorico> listar(String sql, int quantidadeParametros, int id) throws SQLException {
        List<RegistroHistorico> lista = new ArrayList<>();
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            for (int i = 1; i <= quantidadeParametros; i++) {
                comando.setInt(i, id);   // o mesmo id em todos os "?"
            }
            try (ResultSet rs = comando.executeQuery()) {
                while (rs.next()) {
                    lista.add(new RegistroHistorico(
                            rs.getObject("data_hora", LocalDateTime.class),
                            rs.getString("usuario"),
                            rs.getString("entidade"),
                            rs.getInt("id_entidade"),
                            rs.getString("referencia"),
                            TipoAlteracao.valueOf(rs.getString("tipo_alteracao")),
                            rs.getString("campo_alterado"),
                            rs.getString("valor_anterior"),
                            rs.getString("valor_novo"),
                            rs.getString("justificativa")));
                }
            }
        }
        return lista;
    }
}
