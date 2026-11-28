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
    private final boolean responsavelFinanceiro; // true = indicado no vínculo como quem paga a mensalidade

    /** Cria o responsável do contrato sem indicação de responsável financeiro. */
    public ResponsavelContrato(int idResponsavel, String nome, String cpf, String telefone,
                               String endereco, String parentesco, boolean responsavelLegal) {
        this(idResponsavel, nome, cpf, telefone, endereco, parentesco, responsavelLegal, false);
    }

    /**
     * Cria o responsável do contrato com todos os dados, inclusive se é o responsável financeiro.
     */
    public ResponsavelContrato(int idResponsavel, String nome, String cpf, String telefone,
                               String endereco, String parentesco, boolean responsavelLegal,
                               boolean responsavelFinanceiro) {
        this.idResponsavel = idResponsavel;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.endereco = endereco;
        this.parentesco = parentesco;
        this.responsavelLegal = responsavelLegal;
        this.responsavelFinanceiro = responsavelFinanceiro;
    }

    /** Devolve o identificador do responsável. */
    public int getIdResponsavel() { return idResponsavel; }
    /** Devolve o nome. */
    public String getNome() { return nome; }
    /** Devolve o CPF. */
    public String getCpf() { return cpf; }
    /** Devolve o telefone. */
    public String getTelefone() { return telefone; }
    /** Devolve o endereço. */
    public String getEndereco() { return endereco; }
    /** Devolve o grau de parentesco. */
    public String getParentesco() { return parentesco; }
    /** Indica se a pessoa é responsável legal. */
    public boolean isResponsavelLegal() { return responsavelLegal; }
    /** Indica se a pessoa é responsável financeiro. */
    public boolean isResponsavelFinanceiro() { return responsavelFinanceiro; }

    /** Texto que aparece dentro da ComboBox. */
    @Override
    public String toString() {
        return nome + " (" + parentesco + (responsavelLegal ? ", responsável legal)" : ")");
    }
}