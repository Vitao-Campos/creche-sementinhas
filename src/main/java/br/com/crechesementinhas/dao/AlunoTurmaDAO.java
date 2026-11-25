package br.com.crechesementinhas.dao;

import br.com.crechesementinhas.model.AlunoTurma;
import br.com.crechesementinhas.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Consulta de alunos com a turma da matrícula ATIVA (Frente E). Só leitura.
 */
public class AlunoTurmaDAO {

    private static final String SQL_BUSCAR = """
            SELECT a.id_aluno, a.nome, a.data_nascimento,
                   m.id_matricula, m.numero_matricula, m.andarilha, m.motivo_andarilha,
                   t.id_turma, t.nome AS nome_turma, t.idade_minima, t.idade_maxima
              FROM aluno a
              LEFT JOIN matricula m ON m.id_aluno = a.id_aluno AND m.status = 'ATIVA'
              LEFT JOIN turma t ON t.id_turma = m.id_turma
             WHERE a.nome ILIKE ?
               AND (CAST(? AS INT) IS NULL OR t.id_turma = ?)
               AND (? = FALSE OR m.id_matricula IS NOT NULL)
             ORDER BY a.nome
            """;

    /**
     * @param trechoNome      parte do nome ("" = todos)
     * @param idTurma         só os alunos desta turma (null = todas)
     * @param somenteAtivos   só quem tem matrícula ATIVA
     */
    public List<AlunoTurma> buscar(String trechoNome, Integer idTurma, boolean somenteAtivos) throws SQLException {
        List<AlunoTurma> lista = new ArrayList<>();
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(SQL_BUSCAR)) {
            comando.setString(1, "%" + (trechoNome == null ? "" : trechoNome.trim()) + "%");
            if (idTurma == null) {
                comando.setNull(2, Types.INTEGER);
                comando.setNull(3, Types.INTEGER);
            } else {
                comando.setInt(2, idTurma);
                comando.setInt(3, idTurma);
            }
            comando.setBoolean(4, somenteAtivos);
            try (ResultSet rs = comando.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private AlunoTurma mapear(ResultSet rs) throws SQLException {
        AlunoTurma a = new AlunoTurma();
        a.setIdAluno(rs.getInt("id_aluno"));
        a.setNome(rs.getString("nome"));
        a.setDataNascimento(rs.getObject("data_nascimento", LocalDate.class));
        a.setIdMatricula((Integer) rs.getObject("id_matricula"));
        a.setNumeroMatricula(rs.getString("numero_matricula"));
        a.setAndarilha(rs.getBoolean("andarilha"));
        a.setMotivoAndarilha(rs.getString("motivo_andarilha"));
        a.setIdTurma((Integer) rs.getObject("id_turma"));
        a.setNomeTurma(rs.getString("nome_turma"));
        a.setIdadeMinimaMeses(rs.getInt("idade_minima"));
        a.setIdadeMaximaMeses(rs.getInt("idade_maxima"));
        return a;
    }
}
