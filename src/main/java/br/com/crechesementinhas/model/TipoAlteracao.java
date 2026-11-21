package br.com.crechesementinhas.model;

/**
 * Tipos de alteração auditados (RNF-02).
 * Iguais aos valores aceitos pelo CHECK da tabela historico_alteracao.
 */
public enum TipoAlteracao {
    INSERCAO,
    ALTERACAO,
    EXCLUSAO
}