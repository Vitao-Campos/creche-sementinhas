package br.com.crechesementinhas.dao;

import br.com.crechesementinhas.model.Usuario;
import br.com.crechesementinhas.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Acesso à tabela "usuario". Único lugar com SQL relacionado a usuários.
 */
public class UsuarioDAO {

    // Text block (""" ... """): permite escrever SQL em várias linhas, legível.
    // Colunas listadas explicitamente em vez de "SELECT *": se alguém adicionar
    // uma coluna na tabela, esta consulta continua funcionando igual.
    private static final String SQL_BUSCAR_POR_LOGIN = """
            SELECT id_usuario, nome, login, senha_hash, cargo, setor,
                   ativo, ultimo_login, data_criacao
              FROM usuario
             WHERE login = ?
            """;

    private static final String SQL_ATUALIZAR_ULTIMO_LOGIN = """
            UPDATE usuario
               SET ultimo_login = NOW()
             WHERE id_usuario = ?
            """;

    /**
     * Busca um usuário pelo login.
     * Desempenho: O(log n), graças ao índice criado pelo UNIQUE(login).
     *
     * @return Optional com o usuário, ou Optional vazio se o login não existir
     */
    public Optional<Usuario> buscarPorLogin(String login) throws SQLException {
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(SQL_BUSCAR_POR_LOGIN)) {

            comando.setString(1, login); // preenche o 1º "?" com segurança

            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return Optional.of(mapear(resultado));
                }
                return Optional.empty();
            }
        }
    }

    /** Registra a data e hora do login bem-sucedido. */
    public void atualizarUltimoLogin(int idUsuario) throws SQLException {
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(SQL_ATUALIZAR_ULTIMO_LOGIN)) {

            comando.setInt(1, idUsuario);
            comando.executeUpdate();
        }
    }

    /** Converte a linha atual do ResultSet em um objeto Usuario. */
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