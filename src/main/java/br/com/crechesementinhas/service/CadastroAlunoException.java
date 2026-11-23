package br.com.crechesementinhas.service;

/**
 * Erro de regra de negócio no cadastro de aluno, com mensagem pronta
 * para mostrar ao usuário (CPF repetido, data inválida etc.).
 * Não é "checked" porque precisa atravessar o bloco da Transacao,
 * que desfaz tudo (rollback) quando uma exceção sai dele.
 */
public class CadastroAlunoException extends RuntimeException {

    public CadastroAlunoException(String mensagem) {
        super(mensagem);
    }

    public CadastroAlunoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
