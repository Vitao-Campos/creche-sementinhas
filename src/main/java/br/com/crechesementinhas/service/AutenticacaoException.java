package br.com.crechesementinhas.service;

/**
 * Erro de autenticação com mensagem pronta para mostrar ao usuário (EX-03).
 */
public class AutenticacaoException extends Exception {

    public AutenticacaoException(String mensagem) {
        super(mensagem);
    }

    public AutenticacaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}