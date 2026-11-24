package br.com.crechesementinhas.service;

/**
 * Erro de regra de negócio no cadastro de responsável ou no vínculo com o aluno,
 * com mensagem pronta para mostrar ao usuário. Não é "checked" porque precisa
 * atravessar o bloco da Transacao, que faz rollback quando uma exceção sai dele.
 */
public class CadastroResponsavelException extends RuntimeException {

    public CadastroResponsavelException(String mensagem) {
        super(mensagem);
    }

    public CadastroResponsavelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
