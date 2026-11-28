package br.com.crechesementinhas.service;

/** Erro de regra na ficha de anamnese, com mensagem pronta para a tela. */
public class CadastroAnamneseException extends RuntimeException {

    public CadastroAnamneseException(String mensagem) {
        super(mensagem);
    }
}
