package br.com.crechesementinhas.service;

/**
 * Erro de regra de negócio no cadastro de pessoa autorizada, com mensagem pronta
 * para a tela. Não é "checked" porque precisa atravessar a Transacao (rollback).
 */
public class CadastroAutorizadoException extends RuntimeException {

    public CadastroAutorizadoException(String mensagem) {
        super(mensagem);
    }

    public CadastroAutorizadoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
