package br.com.crechesementinhas.dao;

import br.com.crechesementinhas.model.PessoaAutorizada;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Acesso à tabela "pessoa_autorizada". Único lugar com esse SQL. */
public class PessoaAutorizadaDAO {

    private static final String SQL_INSERIR = """
            INSERT INTO pessoa_autorizada (nome, rg, telefone)
            VALUES (?, ?, ?)
            RETURNING id_pessoa_autorizada
            """;

    // A mesma pessoa (mesmo RG e mesmo nome, sem diferenciar maiúsculas) é reaproveitada
    // para outra criança. RG sozinho não basta: RGs de estados diferentes podem coincidir.
    private static final String SQL_BUSCAR_POR_RG_E_NOME = """
            SELECT id_pessoa_autorizada, nome, rg, telefone
              FROM pessoa_autorizada
             WHERE rg = ? AND LOWER(nome) = LOWER(?)
             ORDER BY id_pessoa_autorizada
             LIMIT 1
            """;

    public int inserir(Connection conexao, PessoaAutorizada p) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR)) {
            comando.setString(1, p.getNome());
            comando.setString(2, p.getRg());
            comando.setString(3, p.getTelefone());
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
    }

    public Optional<PessoaAutorizada> buscarPorRgENome(Connection conexao, String rg, String nome)
            throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_BUSCAR_POR_RG_E_NOME)) {
            comando.setString(1, rg);
            comando.setString(2, nome);
            try (ResultSet resultado = comando.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }
                PessoaAutorizada p = new PessoaAutorizada();
                p.setIdPessoaAutorizada(resultado.getInt("id_pessoa_autorizada"));
                p.setNome(resultado.getString("nome"));
                p.setRg(resultado.getString("rg"));
                p.setTelefone(resultado.getString("telefone"));
                return Optional.of(p);
            }
        }
    }
}
