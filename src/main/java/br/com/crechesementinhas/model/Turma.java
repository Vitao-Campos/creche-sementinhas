package br.com.crechesementinhas.model;

/**
 * Turma da creche (tabela "turma").
 * A faixa etária (idade mínima e máxima) é em MESES, a mesma unidade da CalculadoraIdade.
 * Esta classe é usada pela Frente B para escolher a turma na matrícula;
 * a Frente E pode reaproveitá-la na tela de turmas.
 */
public class Turma {

    private Integer idTurma;
    private String nome;
    private int idadeMinimaMeses;
    private int idadeMaximaMeses;
    private int capacidade;
    private String periodo;   // MANHA, TARDE ou INTEGRAL
    private String sala;
    private int matriculasAtivas; // calculado na consulta (não é coluna)

    /** A criança, com esta idade em meses, está dentro da faixa da turma? */
    public boolean aceitaIdade(long idadeEmMeses) {
        return idadeEmMeses >= idadeMinimaMeses && idadeEmMeses <= idadeMaximaMeses;
    }

    /** Devolve a quantidade de vagas livres. */
    public int getVagasLivres() {
        return Math.max(0, capacidade - matriculasAtivas);
    }

    /** Devolve o identificador da turma. */
    public Integer getIdTurma() { return idTurma; }
    /** Define o identificador da turma. */
    public void setIdTurma(Integer idTurma) { this.idTurma = idTurma; }

    /** Devolve o nome. */
    public String getNome() { return nome; }
    /** Define o nome. */
    public void setNome(String nome) { this.nome = nome; }

    /** Devolve a idade mínima, em meses. */
    public int getIdadeMinimaMeses() { return idadeMinimaMeses; }
    /** Define a idade mínima, em meses. */
    public void setIdadeMinimaMeses(int idadeMinimaMeses) { this.idadeMinimaMeses = idadeMinimaMeses; }

    /** Devolve a idade máxima, em meses. */
    public int getIdadeMaximaMeses() { return idadeMaximaMeses; }
    /** Define a idade máxima, em meses. */
    public void setIdadeMaximaMeses(int idadeMaximaMeses) { this.idadeMaximaMeses = idadeMaximaMeses; }

    /** Devolve a capacidade (vagas). */
    public int getCapacidade() { return capacidade; }
    /** Define a capacidade (vagas). */
    public void setCapacidade(int capacidade) { this.capacidade = capacidade; }

    /** Devolve o período. */
    public String getPeriodo() { return periodo; }
    /** Define o período. */
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    /** Devolve a sala. */
    public String getSala() { return sala; }
    /** Define a sala. */
    public void setSala(String sala) { this.sala = sala; }

    /** Devolve a quantidade de matrículas ativas. */
    public int getMatriculasAtivas() { return matriculasAtivas; }
    /** Define a quantidade de matrículas ativas. */
    public void setMatriculasAtivas(int matriculasAtivas) { this.matriculasAtivas = matriculasAtivas; }

    /** É este texto que aparece dentro da ComboBox. */
    @Override
    public String toString() {
        return nome + " (" + idadeMinimaMeses + " a " + idadeMaximaMeses + " meses, "
                + getVagasLivres() + " de " + capacidade + " vagas livres)";
    }
}
