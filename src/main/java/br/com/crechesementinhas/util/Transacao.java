package br.com.crechesementinhas.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Executa várias operações de banco como UMA só (tudo ou nada).
 *
 * Uso:
 *   Transacao.executar(conexao -> {
 *       alunoDAO.atualizar(conexao, aluno);
 *       auditoria.registrarAlteracao(conexao, "aluno", id, "observacao", antigo, novo, motivo);
 *       return null;
 *   });
 */
public final class Transacao {

    /** A "operação" é qualquer bloco de código que recebe a conexão da transação. */
    @FunctionalInterface
    public interface Operacao<T> {
        /** Executa o bloco usando a conexão da transação e devolve o resultado. */
        T executar(Connection conexao) throws SQLException;
    }

    /** Classe só com métodos estáticos: não é instanciada. */
    private Transacao() {
    }

    /**
     * Executa a operação numa transação: confirma tudo se terminar bem e desfaz tudo se houver
     * erro.
     */
    public static <T> T executar(Operacao<T> operacao) throws SQLException {
        try (Connection conexao = ConnectionFactory.getConnection()) {
            conexao.setAutoCommit(false); // a partir daqui, nada é gravado sozinho

            try {
                T resultado = operacao.executar(conexao);
                conexao.commit();         // deu tudo certo: grava TUDO
                return resultado;
            } catch (SQLException | RuntimeException e) {
                conexao.rollback();       // algo falhou: desfaz TUDO
                throw e;                  // e avisa quem chamou
            }
        }
    }
}