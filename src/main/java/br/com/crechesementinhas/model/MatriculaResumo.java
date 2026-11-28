package br.com.crechesementinhas.model;

/**
 * Versão "resumida" de uma matrícula, só para a caixa de seleção da tela.
 * (A classe completa de Matrícula é da Frente B; esta evita conflito no Git.)
 */
public class MatriculaResumo {

    private final int idMatricula;
    private final String numeroMatricula;
    private final String nomeAluno;

    /** Cria o resumo da matrícula (identificador, número e nome da criança). */
    public MatriculaResumo(int idMatricula, String numeroMatricula, String nomeAluno) {
        this.idMatricula = idMatricula;
        this.numeroMatricula = numeroMatricula;
        this.nomeAluno = nomeAluno;
    }

    /** Devolve o identificador da matrícula. */
    public int getIdMatricula() { return idMatricula; }
    /** Devolve o número da matrícula. */
    public String getNumeroMatricula() { return numeroMatricula; }
    /** Devolve o nome do aluno. */
    public String getNomeAluno() { return nomeAluno; }

    /** É este texto que aparece dentro da ComboBox. */
    @Override
    public String toString() {
        return numeroMatricula + " - " + nomeAluno;
    }
}