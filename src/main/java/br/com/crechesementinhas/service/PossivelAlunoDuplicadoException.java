package br.com.crechesementinhas.service;

/**
 * Já existe criança com o mesmo nome e data de nascimento. Não é erro definitivo:
 * a tela pergunta se é outra criança (gêmeos, homônimos) e reenvia com confirmação.
 */
public class PossivelAlunoDuplicadoException extends CadastroAlunoException {

    /** Cria o aviso com a mensagem mostrada na caixa de confirmação. */
    public PossivelAlunoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
