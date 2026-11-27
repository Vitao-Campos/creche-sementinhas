package br.com.crechesementinhas.dao;

import br.com.crechesementinhas.model.Usuario;
import br.com.crechesementinhas.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso à tabela "usuario". Único lugar com SQL relacionado a usuários.
 */
public class UsuarioDAO {

    private static final String COLUNAS = """
            id_usuario, nome, login, senha_hash, cargo, setor,
            ativo, ultimo_login, data_criacao
            """;

    private static final String SQL_BUSCAR_POR_LOGIN =
            "SELECT " + COLUNAS + " FROM usuario WHERE login = ?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT " + COLUNAS + " FROM usuario WHERE id_usuario = ?";

    private static final String SQL_LISTAR_TODOS =
            "SELECT " + COLUNAS + " FROM usuario ORDER BY nome";

    // RETURNING (recurso do PostgreSQL): devolve o ID gerado no mesmo comando,
    // sem precisar de uma segunda consulta.
    private static final String SQL_INSERIR = """
            INSERT INTO usuario (nome, login, senha_hash, cargo, setor, ativo, data_criacao)
            VALUES (?, ?, ?, ?, ?, ?, NOW())
            RETURNING id_usuario
            """;

    private static final String SQL_ATUALIZAR = """
            UPDATE usuario
               SET nome = ?, cargo = ?, setor = ?, ativo = ?
             WHERE id_usuario = ?
            """;

    private static final String SQL_ATUALIZAR_SENHA = """
            UPDATE usuario
               SET senha_hash = ?
             WHERE id_usuario = ?
            """;

    private static final String SQL_ATUALIZAR_ULTIMO_LOGIN = """
            UPDATE usuario
               SET ultimo_login = NOW()
             WHERE id_usuario = ?
            """;

    // ===================== CONSULTAS =====================

    /** Desempenho: O(log n), graças ao índice criado pelo UNIQUE(login). */
    public Optional<Usuario> buscarPorLogin(String login) throws SQLException {
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(SQL_BUSCAR_POR_LOGIN)) {

            comando.setString(1, login);
            return buscarUm(comando);
        }
    }

    /** Desempenho: O(log n), pela chave primária (índice automático). */
    public Optional<Usuario> buscarPorId(int idUsuario) throws SQLException {
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(SQL_BUSCAR_POR_ID)) {

            comando.setInt(1, idUsuario);
            return buscarUm(comando);
        }
    }

    /**
     * Todos os usuários, em ordem alfabética.
     * Desempenho: O(n log n) por causa da ordenação; a creche tem poucos usuários,
     * então é instantâneo.
     */
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(SQL_LISTAR_TODOS);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                usuarios.add(mapear(resultado));
            }
        }
        return usuarios;
    }

    // ===================== GRAVAÇÕES (dentro de uma Transacao) =====================

    /** @return o id_usuario gerado pelo banco */
    public int inserir(Connection conexao, Usuario usuario) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR)) {
            comando.setString(1, usuario.getNome());
            comando.setString(2, usuario.getLogin());
            comando.setString(3, usuario.getSenhaHash());
            comando.setString(4, usuario.getCargo());
            comando.setString(5, usuario.getSetor());
            comando.setBoolean(6, usuario.isAtivo());

            // Com RETURNING, o INSERT devolve uma linha: por isso executeQuery().
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getInt("id_usuario");
            }
        }
    }

    public void atualizar(Connection conexao, Usuario usuario) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_ATUALIZAR)) {
            comando.setString(1, usuario.getNome());
            comando.setString(2, usuario.getCargo());
            comando.setString(3, usuario.getSetor());
            comando.setBoolean(4, usuario.isAtivo());
            comando.setInt(5, usuario.getIdUsuario());
            comando.executeUpdate();
        }
    }

    public void atualizarSenha(Connection conexao, int idUsuario, String novoHash)
            throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_ATUALIZAR_SENHA)) {
            comando.setString(1, novoHash);
            comando.setInt(2, idUsuario);
            comando.executeUpdate();
        }
    }

    /** Registra a data e hora do login bem-sucedido (usado pelo AutenticacaoService). */
    public void atualizarUltimoLogin(int idUsuario) throws SQLException {
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(SQL_ATUALIZAR_ULTIMO_LOGIN)) {

            comando.setInt(1, idUsuario);
            comando.executeUpdate();
        }
    }

    // ===================== AUXILIARES =====================

    /** Executa uma consulta que retorna no máximo uma linha. */
    private Optional<Usuario> buscarUm(PreparedStatement comando) throws SQLException {
        try (ResultSet resultado = comando.executeQuery()) {
            return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
        }
    }

    private Usuario mapear(ResultSet resultado) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(resultado.getInt("id_usuario"));
        usuario.setNome(resultado.getString("nome"));
        usuario.setLogin(resultado.getString("login"));
        usuario.setSenhaHash(resultado.getString("senha_hash"));
        usuario.setCargo(resultado.getString("cargo"));
        usuario.setSetor(resultado.getString("setor"));
        usuario.setAtivo(resultado.getBoolean("ativo"));
        usuario.setUltimoLogin(resultado.getObject("ultimo_login", LocalDateTime.class));
        usuario.setDataCriacao(resultado.getObject("data_criacao", LocalDateTime.class));
        return usuario;
    }
}