package br.com.crechesementinhas.dao;

import br.com.crechesementinhas.model.AlunoResponsavel;
import br.com.crechesementinhas.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso à tabela "aluno_responsavel" (vínculo criança-responsável). Único lugar com esse SQL.
 */
public class AlunoResponsavelDAO {

    private static final String SQL_INSERIR = """
            INSERT INTO aluno_responsavel
                   (id_aluno, id_responsavel, grau_parentesco, responsavel_legal,
                    responsavel_financeiro, data_inicio, data_fim)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id_aluno_responsavel
            """;

    // Base das consultas: traz o nome da criança e do responsável para as telas.
    private static final String SQL_SELECT = """
            SELECT ar.id_aluno_responsavel, ar.id_aluno, a.nome AS nome_aluno,
                   ar.id_responsavel, r.nome AS nome_responsavel, r.telefone AS telefone_responsavel,
                   ar.grau_parentesco, ar.responsavel_legal, ar.responsavel_financeiro,
                   ar.data_inicio, ar.data_fim
              FROM aluno_responsavel ar
              JOIN aluno a ON a.id_aluno = ar.id_aluno
              JOIN responsavel r ON r.id_responsavel = ar.id_responsavel
            """;

    private static final String SQL_BUSCAR_POR_ID = SQL_SELECT + " WHERE ar.id_aluno_responsavel = ?";

    // Vigentes primeiro (data_fim IS NOT NULL = false vem antes), depois por nome.
    private static final String SQL_LISTAR_POR_RESPONSAVEL = SQL_SELECT + """
             WHERE ar.id_responsavel = ?
             ORDER BY ar.data_fim IS NOT NULL, a.nome, ar.data_inicio DESC
            """;

    private static final String SQL_LISTAR_POR_ALUNO = SQL_SELECT + """
             WHERE ar.id_aluno = ?
             ORDER BY ar.data_fim IS NOT NULL, ar.responsavel_legal DESC, r.nome, ar.data_inicio DESC
            """;

    private static final String SQL_ATUALIZAR_DATA_FIM =
            "UPDATE aluno_responsavel SET data_fim = ? WHERE id_aluno_responsavel = ?";

    // Vigente: já começou e não terminou antes de "hoje" (mesma regra de AlunoResponsavel.estaVigente).
    private static final String SQL_EXISTE_VINCULO_VIGENTE = """
            SELECT EXISTS (
                SELECT 1 FROM aluno_responsavel
                 WHERE id_aluno = ? AND id_responsavel = ?
                   AND data_inicio <= ? AND (data_fim IS NULL OR data_fim >= ?)
            )
            """;

    private static final String SQL_CONTAR_LEGAIS_VIGENTES = """
            SELECT COUNT(*) FROM aluno_responsavel
             WHERE id_aluno = ? AND responsavel_legal
               AND data_inicio <= ? AND (data_fim IS NULL OR data_fim >= ?)
            """;

    private static final String SQL_CONTAR_FINANCEIROS_VIGENTES = """
            SELECT COUNT(*) FROM aluno_responsavel
             WHERE id_aluno = ? AND responsavel_financeiro
               AND data_inicio <= ? AND (data_fim IS NULL OR data_fim >= ?)
            """;

    public int inserir(Connection conexao, AlunoResponsavel v) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR)) {
            comando.setInt(1, v.getIdAluno());
            comando.setInt(2, v.getIdResponsavel());
            comando.setString(3, v.getGrauParentesco());
            comando.setBoolean(4, v.isResponsavelLegal());
            comando.setBoolean(5, v.isResponsavelFinanceiro());
            comando.setDate(6, Date.valueOf(v.getDataInicio()));
            comando.setDate(7, v.getDataFim() == null ? null : Date.valueOf(v.getDataFim()));
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
    }

    public Optional<AlunoResponsavel> buscarPorId(Connection conexao, int id) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_BUSCAR_POR_ID)) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        }
    }

    /** Todos os vínculos do responsável (vigentes primeiro). */
    public List<AlunoResponsavel> listarPorResponsavel(int idResponsavel) throws SQLException {
        return listar(SQL_LISTAR_POR_RESPONSAVEL, idResponsavel);
    }

    /** Todos os vínculos da criança (vigentes primeiro, responsáveis legais antes). */
    public List<AlunoResponsavel> listarPorAluno(int idAluno) throws SQLException {
        return listar(SQL_LISTAR_POR_ALUNO, idAluno);
    }

    private List<AlunoResponsavel> listar(String sql, int id) throws SQLException {
        List<AlunoResponsavel> lista = new ArrayList<>();
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    lista.add(mapear(resultado));
                }
            }
        }
        return lista;
    }

    /** Define (ou limpa, com null) a data de fim do vínculo. */
    public void atualizarDataFim(Connection conexao, int idVinculo, LocalDate dataFim) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_ATUALIZAR_DATA_FIM)) {
            comando.setDate(1, dataFim == null ? null : Date.valueOf(dataFim));
            comando.setInt(2, idVinculo);
            comando.executeUpdate();
        }
    }

    /** Este responsável já tem vínculo vigente com esta criança? */
    public boolean existeVinculoVigente(Connection conexao, int idAluno, int idResponsavel, LocalDate hoje)
            throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_EXISTE_VINCULO_VIGENTE)) {
            comando.setInt(1, idAluno);
            comando.setInt(2, idResponsavel);
            comando.setDate(3, Date.valueOf(hoje));
            comando.setDate(4, Date.valueOf(hoje));
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getBoolean(1);
            }
        }
    }

    /** Quantos responsáveis legais vigentes a criança tem? (RN-04) */
    public int contarLegaisVigentes(Connection conexao, int idAluno, LocalDate hoje) throws SQLException {
        return contar(conexao, SQL_CONTAR_LEGAIS_VIGENTES, idAluno, hoje);
    }

    /** Quantos responsáveis financeiros vigentes a criança tem? (deve ser no máximo 1) */
    public int contarFinanceirosVigentes(Connection conexao, int idAluno, LocalDate hoje) throws SQLException {
        return contar(conexao, SQL_CONTAR_FINANCEIROS_VIGENTES, idAluno, hoje);
    }

    private int contar(Connection conexao, String sql, int idAluno, LocalDate hoje) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, idAluno);
            comando.setDate(2, Date.valueOf(hoje));
            comando.setDate(3, Date.valueOf(hoje));
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
    }

    private AlunoResponsavel mapear(ResultSet rs) throws SQLException {
        AlunoResponsavel v = new AlunoResponsavel();
        v.setIdAlunoResponsavel(rs.getInt("id_aluno_responsavel"));
        v.setIdAluno(rs.getInt("id_aluno"));
        v.setNomeAluno(rs.getString("nome_aluno"));
        v.setIdResponsavel(rs.getInt("id_responsavel"));
        v.setNomeResponsavel(rs.getString("nome_responsavel"));
        v.setTelefoneResponsavel(rs.getString("telefone_responsavel"));
        v.setGrauParentesco(rs.getString("grau_parentesco"));
        v.setResponsavelLegal(rs.getBoolean("responsavel_legal"));
        v.setResponsavelFinanceiro(rs.getBoolean("responsavel_financeiro"));
        v.setDataInicio(rs.getObject("data_inicio", LocalDate.class));
        v.setDataFim(rs.getObject("data_fim", LocalDate.class));
        return v;
    }
}
