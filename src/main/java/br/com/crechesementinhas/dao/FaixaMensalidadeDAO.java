package br.com.crechesementinhas.dao;

import br.com.crechesementinhas.model.FaixaMensalidade;
import br.com.crechesementinhas.util.ConnectionFactory;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso à tabela "faixa_mensalidade" (RN-07).
 */
public class FaixaMensalidadeDAO {

    private static final String SQL_LISTAR = """
            SELECT id_faixa_mensalidade, descricao, idade_minima_anos, idade_maxima_anos, valor
              FROM faixa_mensalidade
             ORDER BY idade_minima_anos
            """;

    public List<FaixaMensalidade> listar() throws SQLException {
        try (Connection conexao = ConnectionFactory.getConnection()) {
            return listar(conexao);
        }
    }

    public List<FaixaMensalidade> listar(Connection conexao) throws SQLException {
        List<FaixaMensalidade> faixas = new ArrayList<>();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_LISTAR);
             ResultSet rs = comando.executeQuery()) {
            while (rs.next()) {
                faixas.add(new FaixaMensalidade(
                        rs.getInt("id_faixa_mensalidade"),
                        rs.getString("descricao"),
                        rs.getInt("idade_minima_anos"),
                        (Integer) rs.getObject("idade_maxima_anos"),
                        rs.getBigDecimal("valor")));
            }
        }
        return faixas;
    }

    /** Valor atual de uma faixa, travando a linha para o reajuste. */
    public BigDecimal buscarValorETravar(Connection conexao, int idFaixa) throws SQLException {
        String sql = "SELECT valor FROM faixa_mensalidade WHERE id_faixa_mensalidade = ? FOR UPDATE";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, idFaixa);
            try (ResultSet rs = comando.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : null;
            }
        }
    }

    /** Reajusta o valor de uma faixa. Usa a conexão RECEBIDA (junto com a auditoria). */
    public void atualizarValor(Connection conexao, int idFaixa, BigDecimal valor) throws SQLException {
        String sql = "UPDATE faixa_mensalidade SET valor = ? WHERE id_faixa_mensalidade = ?";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setBigDecimal(1, valor);
            comando.setInt(2, idFaixa);
            comando.executeUpdate();
        }
    }
}
