package br.com.crechesementinhas.dao;

import br.com.crechesementinhas.model.Documento;
import br.com.crechesementinhas.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso ao banco para DOCUMENTOS anexados (RF-06).
 * O arquivo em si fica numa pasta; no banco guardamos só o caminho.
 */
public class DocumentoDAO {

    public void inserir(Documento d) throws SQLException {
        String sql = """
                INSERT INTO documento (id_matricula, nome, tipo, arquivo, data, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, d.getIdMatricula());
            ps.setString(2, d.getNome());
            ps.setString(3, d.getTipo());
            ps.setString(4, d.getArquivo());
            ps.setDate(5, Date.valueOf(d.getData()));
            ps.setString(6, d.getStatus());
            ps.executeUpdate();
        }
    }
}