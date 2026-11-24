package br.com.crechesementinhas.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;

import br.com.crechesementinhas.model.Matricula;
import br.com.crechesementinhas.util.ConnectionFactory;

public class MatriculaDAO {

    public void inserir(Connection conn, Matricula matricula) throws SQLException {
        String sql = """
            INSERT INTO matricula (numero_matricula, id_aluno, id_turma, status,
                                   data_matricula, data_encerramento, andarilha, motivo_andarilha)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, matricula.getNumeroMatricula());
            stmt.setInt(2, matricula.getIdAluno());
            stmt.setInt(3, matricula.getIdTurma());
            stmt.setString(4, matricula.getStatus());
            stmt.setDate(5, Date.valueOf(matricula.getDataMatricula()));

            if (matricula.getDataEncerramento() != null) {
                stmt.setDate(6, Date.valueOf(matricula.getDataEncerramento()));
            } else {
                stmt.setNull(6, Types.DATE);
            }

            stmt.setBoolean(7, matricula.isAndarilha());
            stmt.setString(8, matricula.getMotivoAndarilha());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    matricula.setIdMatricula(rs.getInt(1));
                }
            }
        }
    }

    // RF-04 / RN-12: Encerramento (desligamento) da matrícula
    public void encerrarMatricula(Connection conn, int idMatricula, LocalDate dataEncerramento) throws SQLException {
        String sql = """
            UPDATE matricula
            SET status = 'ENCERRADA', data_encerramento = ?
            WHERE id_matricula = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(dataEncerramento));
            stmt.setInt(2, idMatricula);
            stmt.executeUpdate();
        }
    }

    public Matricula buscarAtivaPorAluno(int idAluno) throws SQLException {
        String sql = "SELECT * FROM matricula WHERE id_aluno = ? AND status = 'ATIVA'";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAluno);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public int contarAtivosNaTurma(int idTurma) throws SQLException {
        String sql = "SELECT COUNT(*) FROM matricula WHERE id_turma = ? AND status = 'ATIVA'";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idTurma);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Matricula mapear(ResultSet rs) throws SQLException {
        Matricula m = new Matricula();
        m.setIdMatricula(rs.getInt("id_matricula"));
        m.setNumeroMatricula(rs.getString("numero_matricula"));
        m.setIdAluno(rs.getInt("id_aluno"));
        m.setIdTurma(rs.getInt("id_turma"));
        m.setStatus(rs.getString("status"));

        Date dm = rs.getDate("data_matricula");
        if (dm != null) m.setDataMatricula(dm.toLocalDate());

        Date de = rs.getDate("data_encerramento");
        if (de != null) m.setDataEncerramento(de.toLocalDate());

        m.setAndarilha(rs.getBoolean("andarilha"));
        m.setMotivoAndarilha(rs.getString("motivo_andarilha"));
        return m;
    }
}