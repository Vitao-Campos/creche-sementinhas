package br.com.crechesementinhas.model;

/**
 * Sexo da criança. O banco guarda só "F" ou "M" (CHECK da tabela aluno).
 */
public enum Sexo {
    F("Feminino"),
    M("Masculino");

    private final String descricao;

    Sexo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** É este texto que aparece dentro da ComboBox. */
    @Override
    public String toString() {
        return descricao;
    }
}
