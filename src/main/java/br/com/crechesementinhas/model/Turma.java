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

    public int getVagasLivres() {
        return Math.max(0, capacidade - matriculasAtivas);
    }

    public Integer getIdTurma() { return idTurma; }
    public void setIdTurma(Integer idTurma) { this.idTurma = idTurma; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getIdadeMinimaMeses() { return idadeMinimaMeses; }
    public void setIdadeMinimaMeses(int idadeMinimaMeses) { this.idadeMinimaMeses = idadeMinimaMeses; }

    public int getIdadeMaximaMeses() { return idadeMaximaMeses; }
    public void setIdadeMaximaMeses(int idadeMaximaMeses) { this.idadeMaximaMeses = idadeMaximaMeses; }

    public int getCapacidade() { return capacidade; }
    public void setCapacidade(int capacidade) { this.capacidade = capacidade; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public String getSala() { return sala; }
    public void setSala(String sala) { this.sala = sala; }

    public int getMatriculasAtivas() { return matriculasAtivas; }
    public void setMatriculasAtivas(int matriculasAtivas) { this.matriculasAtivas = matriculasAtivas; }

    /** É este texto que aparece dentro da ComboBox. */
    @Override
    public String toString() {
        return nome + " (" + idadeMinimaMeses + " a " + idadeMaximaMeses + " meses, "
                + getVagasLivres() + " de " + capacidade + " vagas livres)";
    }
}
