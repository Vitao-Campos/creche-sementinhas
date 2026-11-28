package br.com.crechesementinhas.service;

/** Erro de regra na ficha de anamnese, com mensagem pronta para a tela. */
public class CadastroAnamneseException extends RuntimeException {

    /** Cria o erro com a mensagem pronta para a tela. */
    public CadastroAnamneseException(String mensagem) {
        super(mensagem);
    }
}
