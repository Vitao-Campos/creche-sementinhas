package br.com.crechesementinhas.model;

/**
 * O que a pessoa autorizada pode fazer pela criança (RF-02, RN-04).
 * Os nomes (RETIRADA, EMERGENCIA, AMBOS) são os aceitos pelo CHECK do banco.
 */
public enum TipoAutorizacao {
    RETIRADA("Retirada"),
    EMERGENCIA("Emergência"),
    AMBOS("Retirada e emergência");

    private final String descricao;

    /** Cria o tipo com o texto mostrado na tela. */
    TipoAutorizacao(String descricao) {
        this.descricao = descricao;
    }

    /** Devolve a descrição. */
    public String getDescricao() {
        return descricao;
    }

    /** Texto do tipo nas listas: a descrição. */
    @Override
    public String toString() {
        return descricao;
    }
}
