package br.com.crechesementinhas.dao;

import br.com.crechesementinhas.model.Responsavel;
import br.com.crechesementinhas.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso à tabela "responsavel". Único lugar com SQL de responsável.
 * Os métodos que gravam recebem a Connection de uma Transacao.
 */
public class ResponsavelDAO {

    private static final String COLUNAS =
            "id_responsavel, nome, cpf, email, telefone, cep, endereco, profissao";

    private static final String SQL_INSERIR = """
            INSERT INTO responsavel (nome, cpf, email, telefone, cep, endereco, profissao)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id_responsavel
            """;

    private static final String SQL_ATUALIZAR = """
            UPDATE responsavel
               SET nome = ?, cpf = ?, email = ?, telefone = ?,
                   cep = ?, endereco = ?, profissao = ?
             WHERE id_responsavel = ?
            """;

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT " + COLUNAS + " FROM responsavel WHERE id_responsavel = ?";

    // Busca por parte do nome OU por dígitos do CPF (a secretaria costuma ter um dos dois).
    private static final String SQL_LISTAR = "SELECT " + COLUNAS + """

              FROM responsavel
             WHERE nome ILIKE ? OR cpf LIKE ?
             ORDER BY nome
             LIMIT 500
            """;

    private static final String SQL_EXISTE_CPF = """
            SELECT EXISTS (
                SELECT 1 FROM responsavel
                 WHERE cpf = ? AND id_responsavel <> ?
            )
            """;

    /** Grava um responsável novo e devolve o id gerado. Não fecha a conexão recebida. */
    public int inserir(Connection conexao, Responsavel r) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR)) {
            preencher(comando, r);
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
    }

    /** Atualiza todos os campos. Não fecha a conexão recebida. */
    public void atualizar(Connection conexao, Responsavel r) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_ATUALIZAR)) {
            preencher(comando, r);
            comando.setInt(8, r.getIdResponsavel());
            comando.executeUpdate();
        }
    }

    /** Busca um responsável pelo identificador. */
    public Optional<Responsavel> buscarPorId(Connection conexao, int id) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_BUSCAR_POR_ID)) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        }
    }

    /**
     * Lista em ordem alfabética, filtrando por parte do nome ou por dígitos do CPF.
     *
     * @param busca vazio ou null lista todos
     */
    public List<Responsavel> listar(String busca) throws SQLException {
        String texto = busca == null ? "" : busca.trim();
        String digitos = texto.replaceAll("\\D", "");
        List<Responsavel> lista = new ArrayList<>();

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(SQL_LISTAR)) {

            comando.setString(1, "%" + escaparCuringas(texto) + "%");
            // Sem dígitos, o padrão do CPF fica null (NULL no OR = não casa) e só o nome filtra;
            // um "%%" aqui casaria todos os CPFs e a busca por nome nunca filtraria nada.
            comando.setString(2, digitos.isEmpty() ? null : "%" + digitos + "%");
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    lista.add(mapear(resultado));
                }
            }
        }
        return lista;
    }

    /** Já existe responsável com este CPF? (único no banco). idIgnorado = o próprio na edição; null no cadastro. */
    public boolean existeCpf(Connection conexao, String cpf, Integer idIgnorado) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_EXISTE_CPF)) {
            comando.setString(1, cpf);
            comando.setInt(2, idIgnorado == null ? -1 : idIgnorado);
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getBoolean(1);
            }
        }
    }

    /**
     * Preenche os campos de dados do responsável no comando (iguais no cadastro e na alteração).
     */
    private void preencher(PreparedStatement comando, Responsavel r) throws SQLException {
        comando.setString(1, r.getNome());
        comando.setString(2, r.getCpf());
        comando.setString(3, r.getEmail());
        comando.setString(4, r.getTelefone());
        comando.setString(5, r.getCep());
        comando.setString(6, r.getEndereco());
        comando.setString(7, r.getProfissao());
    }

    /** Protege os caracteres especiais da busca (% e _) para serem procurados como texto comum. */
    private String escaparCuringas(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /** Converte a linha do resultado da consulta em um objeto Responsavel. */
    private Responsavel mapear(ResultSet rs) throws SQLException {
        Responsavel r = new Responsavel();
        r.setIdResponsavel(rs.getInt("id_responsavel"));
        r.setNome(rs.getString("nome"));
        r.setCpf(rs.getString("cpf"));
        r.setEmail(rs.getString("email"));
        r.setTelefone(rs.getString("telefone"));
        r.setCep(rs.getString("cep"));
        r.setEndereco(rs.getString("endereco"));
        r.setProfissao(rs.getString("profissao"));
        return r;
    }
}
