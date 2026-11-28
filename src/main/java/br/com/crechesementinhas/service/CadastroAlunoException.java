package br.com.crechesementinhas.service;

/**
 * Erro de regra de negócio no cadastro de aluno, com mensagem pronta
 * para mostrar ao usuário (CPF repetido, data inválida etc.).
 * Não é "checked" porque precisa atravessar o bloco da Transacao,
 * que desfaz tudo (rollback) quando uma exceção sai dele.
 */
public class CadastroAlunoException extends RuntimeException {

    /** Cria o erro com a mensagem pronta para a tela. */
    public CadastroAlunoException(String mensagem) {
        super(mensagem);
    }

    /** Cria o erro com a mensagem e a causa original. */
    public CadastroAlunoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
