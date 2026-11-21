package br.com.crechesementinhas.model;

/**
 * Motivos de falha no login (EX-03).
 * Os nomes são iguais aos aceitos pelo CHECK da tabela log_acesso.
 */
public enum MotivoFalhaLogin {
    USUARIO_INEXISTENTE,
    SENHA_INCORRETA,
    USUARIO_INATIVO
}