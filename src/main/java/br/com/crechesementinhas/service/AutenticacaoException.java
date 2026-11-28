package br.com.crechesementinhas.service;

/**
 * Erro de autenticação com mensagem pronta para mostrar ao usuário (EX-03).
 */
public class AutenticacaoException extends Exception {

    /** Cria o erro de autenticação com a mensagem para a tela. */
    public AutenticacaoException(String mensagem) {
        super(mensagem);
    }

    /** Cria o erro de autenticação com a mensagem e a causa original. */
    public AutenticacaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}