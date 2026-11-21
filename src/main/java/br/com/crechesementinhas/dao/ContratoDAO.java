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
}