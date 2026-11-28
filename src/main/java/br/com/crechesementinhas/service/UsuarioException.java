package br.com.crechesementinhas.service;

/**
 * Erro de regra de negócio no gerenciamento de usuários,
 * com mensagem pronta para mostrar na tela.
 */
public class UsuarioException extends Exception {

    /** Cria o erro com a mensagem pronta para a tela. */
    public UsuarioException(String mensagem) {
        super(mensagem);
    }

    /** Cria o erro com a mensagem e a causa original. */
    public UsuarioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}