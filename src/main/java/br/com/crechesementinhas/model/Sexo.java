package br.com.crechesementinhas.model;

/**
 * Sexo da criança. O banco guarda só "F" ou "M" (CHECK da tabela aluno).
 */
public enum Sexo {
    F("Feminino"),
    M("Masculino");

    private final String descricao;

    /** Cria o valor com o texto mostrado na tela. */
    Sexo(String descricao) {
        this.descricao = descricao;
    }

    /** Devolve a descrição. */
    public String getDescricao() {
        return descricao;
    }

    /** É este texto que aparece dentro da ComboBox. */
    @Override
    public String toString() {
        return descricao;
    }
}
