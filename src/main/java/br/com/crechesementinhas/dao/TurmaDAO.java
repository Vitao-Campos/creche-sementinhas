package br.com.crechesementinhas.dao;

import br.com.crechesementinhas.model.Turma;
import br.com.crechesementinhas.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Consulta das turmas (tabela "turma"), já com a contagem de matrículas ATIVAS.
 * Só leitura: o cadastro de turmas não faz parte da Frente B.
 */
public class TurmaDAO {

    private static final String SQL_BASE = """
            SELECT t.id_turma, t.nome, t.idade_minima, t.idade_maxima, t.capacidade,
                   t.periodo, t.sala,
                   (SELECT COUNT(*) FROM matricula m
                     WHERE m.id_turma = t.id_turma AND m.status = 'ATIVA') AS ativas
              FROM turma t
            """;

    /** Todas as turmas, em ordem de idade mínima. */
    public List<Turma> listar() throws SQLException {
        List<Turma> turmas = new ArrayList<>();
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(
                     SQL_BASE + " ORDER BY t.idade_minima, t.nome");
             ResultSet resultado = comando.executeQuery()) {
            while (resultado.next()) {
                turmas.add(mapear(resultado));
            }
        }
        return turmas;
    }

    /**
     * Busca a turma dentro de uma transação e TRAVA a linha (FOR UPDATE):
     * duas secretárias matriculando ao mesmo tempo não estouram a capacidade.
     */
    public Optional<Turma> buscarETravar(Connection conexao, int idTurma) throws SQLException {
        String sql = """
                SELECT t.id_turma, t.nome, t.idade_minima, t.idade_maxima, t.capacidade,
                       t.periodo, t.sala,
                       (SELECT COUNT(*) FROM matricula m
                         WHERE m.id_turma = t.id_turma AND m.status = 'ATIVA') AS ativas
                  FROM turma t
                 WHERE t.id_turma = ?
                   FOR UPDATE OF t
                """;
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, idTurma);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        }
    }

    private Turma mapear(ResultSet rs) throws SQLException {
        Turma t = new Turma();
        t.setIdTurma(rs.getInt("id_turma"));
        t.setNome(rs.getString("nome"));
        t.setIdadeMinimaMeses(rs.getInt("idade_minima"));
        t.setIdadeMaximaMeses(rs.getInt("idade_maxima"));
        t.setCapacidade(rs.getInt("capacidade"));
        t.setPeriodo(rs.getString("periodo"));
        t.setSala(rs.getString("sala"));
        t.setMatriculasAtivas(rs.getInt("ativas"));
        return t;
    }
}
