package br.com.crechesementinhas.dao;

import br.com.crechesementinhas.model.Matricula;
import br.com.crechesementinhas.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso à tabela "matricula". Único lugar com SQL de matrícula.
 * Os métodos que gravam recebem a Connection de uma Transacao.
 */
public class MatriculaDAO {

    private static final String SQL_SELECT = """
            SELECT m.id_matricula, m.numero_matricula, m.id_aluno, m.id_turma, m.status,
                   m.data_matricula, m.data_encerramento, m.andarilha, m.motivo_andarilha,
                   t.nome AS nome_turma
              FROM matricula m
              JOIN turma t ON t.id_turma = m.id_turma
            """;

    /** Grava a matrícula e preenche o id gerado no próprio objeto. */
    public void inserir(Connection conexao, Matricula matricula) throws SQLException {
        String sql = """
                INSERT INTO matricula (numero_matricula, id_aluno, id_turma, status,
                                       data_matricula, data_encerramento, andarilha, motivo_andarilha)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id_matricula
                """;
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, matricula.getNumeroMatricula());
            stmt.setInt(2, matricula.getIdAluno());
            stmt.setInt(3, matricula.getIdTurma());
            stmt.setString(4, matricula.getStatus());
            stmt.setDate(5, Date.valueOf(matricula.getDataMatricula()));
            stmt.setObject(6, matricula.getDataEncerramento());
            stmt.setBoolean(7, matricula.isAndarilha());
            stmt.setString(8, matricula.getMotivoAndarilha());
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                matricula.setIdMatricula(rs.getInt(1));
            }
        }
    }

    /**
     * Próximo número de matrícula do ano, no formato MAT-2026-001.
     * Trava a tabela para dois cadastros simultâneos não gerarem o mesmo número.
     */
    public String proximoNumero(Connection conexao, int ano) throws SQLException {
        try (Statement trava = conexao.createStatement()) {
            trava.execute("LOCK TABLE matricula IN SHARE ROW EXCLUSIVE MODE");
        }
        String prefixo = "MAT-" + ano + "-";
        String sql = """
                SELECT COALESCE(MAX(CAST(SUBSTRING(numero_matricula FROM ?) AS INT)), 0)
                  FROM matricula
                 WHERE numero_matricula ~ ?
                """;
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, prefixo.length() + 1);
            stmt.setString(2, "^" + prefixo + "[0-9]+$");
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return prefixo + String.format("%03d", rs.getInt(1) + 1);
            }
        }
    }

    /** RF-10: encerra a matrícula (desligamento). */
    public void encerrar(Connection conexao, int idMatricula, LocalDate dataEncerramento) throws SQLException {
        String sql = """
                UPDATE matricula
                   SET status = 'ENCERRADA', data_encerramento = ?
                 WHERE id_matricula = ? AND status = 'ATIVA'
                """;
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(dataEncerramento));
            stmt.setInt(2, idMatricula);
            if (stmt.executeUpdate() != 1) {
                throw new SQLException("A matrícula não está mais ATIVA (foi alterada por outro usuário?).");
            }
        }
    }

    /** Matrícula ATIVA do aluno, se houver. */
    public Optional<Matricula> buscarAtivaPorAluno(int idAluno) throws SQLException {
        try (Connection conexao = ConnectionFactory.getConnection()) {
            return buscarAtivaPorAluno(conexao, idAluno);
        }
    }

    public Optional<Matricula> buscarAtivaPorAluno(Connection conexao, int idAluno) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement(
                SQL_SELECT + " WHERE m.id_aluno = ? AND m.status = 'ATIVA'")) {
            stmt.setInt(1, idAluno);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    /** Todas as matrículas do aluno (histórico), da mais recente para a mais antiga. */
    public List<Matricula> listarPorAluno(int idAluno) throws SQLException {
        List<Matricula> lista = new ArrayList<>();
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(
                     SQL_SELECT + " WHERE m.id_aluno = ? ORDER BY m.data_matricula DESC, m.id_matricula DESC")) {
            stmt.setInt(1, idAluno);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private Matricula mapear(ResultSet rs) throws SQLException {
        Matricula m = new Matricula();
        m.setIdMatricula(rs.getInt("id_matricula"));
        m.setNumeroMatricula(rs.getString("numero_matricula"));
        m.setIdAluno(rs.getInt("id_aluno"));
        m.setIdTurma(rs.getInt("id_turma"));
        m.setStatus(rs.getString("status"));
        m.setDataMatricula(rs.getObject("data_matricula", LocalDate.class));
        m.setDataEncerramento(rs.getObject("data_encerramento", LocalDate.class));
        m.setAndarilha(rs.getBoolean("andarilha"));
        m.setMotivoAndarilha(rs.getString("motivo_andarilha"));
        m.setNomeTurma(rs.getString("nome_turma"));
        return m;
    }
}
