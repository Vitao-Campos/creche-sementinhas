package br.com.crechesementinhas.dao;

import br.com.crechesementinhas.model.MotivoFalhaLogin;
import br.com.crechesementinhas.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Grava as tentativas de login na tabela log_acesso (EX-03).
 */
public class LogAcessoDAO {

    private static final int TAMANHO_MAXIMO_LOGIN = 40; // igual ao VARCHAR(40)

    private static final String SQL_REGISTRAR = """
            INSERT INTO log_acesso (login_informado, id_usuario, sucesso, motivo, computador)
            VALUES (?, ?, ?, ?, ?)
            """;

    /**
     * @param idUsuario null quando o login digitado não existe
     * @param motivo    null quando o acesso teve sucesso
     */
    public void registrar(String loginInformado, Integer idUsuario,
                          boolean sucesso, MotivoFalhaLogin motivo) throws SQLException {

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(SQL_REGISTRAR)) {

            comando.setString(1, limitarTamanho(loginInformado));

            // setObject com Types.INTEGER aceita null; setInt(int) não aceitaria.
            comando.setObject(2, idUsuario, Types.INTEGER);

            comando.setBoolean(3, sucesso);
            comando.setString(4, motivo == null ? null : motivo.name());
            comando.setString(5, System.getenv("COMPUTERNAME")); // nome do PC no Windows

            comando.executeUpdate();
        }
    }

    // Evita erro no banco se alguém digitar um login enorme no campo.
    private String limitarTamanho(String texto) {
        return texto.length() <= TAMANHO_MAXIMO_LOGIN
                ? texto
                : texto.substring(0, TAMANHO_MAXIMO_LOGIN);
    }
}