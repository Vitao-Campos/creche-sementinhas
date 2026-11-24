package br.com.crechesementinhas.model;

/**
 * Versão "resumida" de uma matrícula, só para a caixa de seleção da tela.
 * (A classe completa de Matrícula é da Frente B; esta evita conflito no Git.)
 */
public class MatriculaResumo {

    private final int idMatricula;
    private final String numeroMatricula;
    private final String nomeAluno;

    public MatriculaResumo(int idMatricula, String numeroMatricula, String nomeAluno) {
        this.idMatricula = idMatricula;
        this.numeroMatricula = numeroMatricula;
        this.nomeAluno = nomeAluno;
    }

    public int getIdMatricula() { return idMatricula; }
    public String getNumeroMatricula() { return numeroMatricula; }
    public String getNomeAluno() { return nomeAluno; }

    /** É este texto que aparece dentro da ComboBox. */
    @Override
    public String toString() {
        return numeroMatricula + " - " + nomeAluno;
    }
}