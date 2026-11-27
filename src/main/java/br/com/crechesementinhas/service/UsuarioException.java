package br.com.crechesementinhas.service;

/**
 * Erro de regra de negócio no gerenciamento de usuários,
 * com mensagem pronta para mostrar na tela.
 */
public class UsuarioException extends Exception {

    public UsuarioException(String mensagem) {
        super(mensagem);
    }

    public UsuarioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}