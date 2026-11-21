package br.com.crechesementinhas.dao;

import br.com.crechesementinhas.model.Contrato;
import br.com.crechesementinhas.model.DadosContrato;
import br.com.crechesementinhas.model.MatriculaResumo;
import br.com.crechesementinhas.model.ResponsavelContrato;
import br.com.crechesementinhas.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso ao banco para CONTRATOS (único lugar da Frente F com SQL de contrato).
 * Sempre usamos PreparedStatement com "?" para evitar SQL Injection.
 */
public class ContratoDAO {

    /** Matrículas ATIVAS, para a ComboBox do topo da tela. */
    public List<MatriculaResumo> listarMatriculasAtivas() throws SQLException {
        String sql = """
                SELECT m.id_matricula, m.numero_matricula, a.nome
                FROM matricula m
                JOIN aluno a ON a.id_aluno = m.id_aluno
                WHERE m.status = 'ATIVA'
                ORDER BY a.nome
                """;
        List<MatriculaResumo> lista = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new MatriculaResumo(
                        rs.getInt("id_matricula"),
                        rs.getString("numero_matricula"),
                        rs.getString("nome")));
            }
        }
        return lista;
    }
    
    /**
     * RF-05: busca os dados JÁ cadastrados (aluno, turma e responsáveis)
     * para preencher o contrato automaticamente, sem digitar de novo.
     */
    public DadosContrato buscarDadosContrato(int idMatricula) throws SQLException {
        String sqlMatricula = """
                SELECT m.id_matricula, m.numero_matricula, m.status,
                       a.id_aluno, a.nome AS nome_aluno, a.data_nascimento,
                       t.nome AS nome_turma, t.periodo
                FROM matricula m
                JOIN aluno a ON a.id_aluno = m.id_aluno
                JOIN turma t ON t.id_turma = m.id_turma
                WHERE m.id_matricula = ?
                """;
        // Só vínculos que ainda valem (data_fim vazia ou no futuro).
        String sqlResponsaveis = """
                SELECT r.id_responsavel, r.nome, r.cpf, r.telefone, r.endereco,
                       ar.grau_parentesco, ar.responsavel_legal
                FROM aluno_responsavel ar
                JOIN responsavel r ON r.id_responsavel = ar.id_responsavel
                WHERE ar.id_aluno = ?
                  AND (ar.data_fim IS NULL OR ar.data_fim >= CURRENT_DATE)
                ORDER BY ar.responsavel_legal DESC, r.nome
                """;

        try (Connection con = ConnectionFactory.getConnection()) {
            DadosContrato dados = null;

            try (PreparedStatement ps = con.prepareStatement(sqlMatricula)) {
                ps.setInt(1, idMatricula);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        dados = new DadosContrato();
                        dados.setIdMatricula(rs.getInt("id_matricula"));
                        dados.setNumeroMatricula(rs.getString("numero_matricula"));
                        dados.setStatusMatricula(rs.getString("status"));
                        dados.setIdAluno(rs.getInt("id_aluno"));
                        dados.setNomeAluno(rs.getString("nome_aluno"));
                        dados.setDataNascimentoAluno(rs.getDate("data_nascimento").toLocalDate());
                        dados.setNomeTurma(rs.getString("nome_turma"));
                        dados.setPeriodoTurma(rs.getString("periodo"));
                    }
                }
            }

            if (dados == null) {
                return null; // matrícula não existe
            }

            try (PreparedStatement ps = con.prepareStatement(sqlResponsaveis)) {
                ps.setInt(1, dados.getIdAluno());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        dados.getResponsaveis().add(new ResponsavelContrato(
                                rs.getInt("id_responsavel"),
                                rs.getString("nome"),
                                rs.getString("cpf"),
                                rs.getString("telefone"),
                                rs.getString("endereco"),
                                rs.getString("grau_parentesco"),
                                rs.getBoolean("responsavel_legal")));
                    }
                }
            }
            return dados;
        }
    }
}