package br.com.crechesementinhas.model;

/**
 * Dados de um responsável que aparecem no contrato.
 * (A classe completa de Responsável é da Frente C; esta evita conflito no Git.)
 */
public class ResponsavelContrato {

    private final int idResponsavel;
    private final String nome;
    private final String cpf;
    private final String telefone;
    private final String endereco;
    private final String parentesco;
    private final boolean responsavelLegal; // true = pode assinar (RN-01, RN-05)

    public ResponsavelContrato(int idResponsavel, String nome, String cpf, String telefone,
                               String endereco, String parentesco, boolean responsavelLegal) {
        this.idResponsavel = idResponsavel;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.endereco = endereco;
        this.parentesco = parentesco;
        this.responsavelLegal = responsavelLegal;
    }

    public int getIdResponsavel() { return idResponsavel; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getTelefone() { return telefone; }
    public String getEndereco() { return endereco; }
    public String getParentesco() { return parentesco; }
    public boolean isResponsavelLegal() { return responsavelLegal; }

    /** Texto que aparece dentro da ComboBox. */
    @Override
    public String toString() {
        return nome + " (" + parentesco + (responsavelLegal ? ", responsável legal)" : ")");
    }
}