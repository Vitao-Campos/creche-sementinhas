package br.com.crechesementinhas.service;

/**
 * Erro de regra de negócio no cadastro de pessoa autorizada, com mensagem pronta
 * para a tela. Não é "checked" porque precisa atravessar a Transacao (rollback).
 */
public class CadastroAutorizadoException extends RuntimeException {

    /** Cria o erro com a mensagem pronta para a tela. */
    public CadastroAutorizadoException(String mensagem) {
        super(mensagem);
    }

    /** Cria o erro com a mensagem e a causa original. */
    public CadastroAutorizadoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
