package br.com.crechesementinhas.model;

import java.time.LocalDate;

/**
 * Matrícula de uma criança numa turma (tabela "matricula").
 */
public class Matricula {

    public static final String ATIVA = "ATIVA";
    public static final String TRANCADA = "TRANCADA";
    public static final String ENCERRADA = "ENCERRADA";

    private Integer idMatricula;
    private String numeroMatricula;
    private Integer idAluno;
    private Integer idTurma;
    private String status; // Valores permitidos no banco: 'ATIVA', 'TRANCADA', 'ENCERRADA'
    private LocalDate dataMatricula;
    private LocalDate dataEncerramento;
    private boolean andarilha;
    private String motivoAndarilha;
    private LocalDate andarilhaAte; // RF-11: prazo previsto da andarilha (V7)
    private String nomeTurma; // vem da consulta com JOIN (não é coluna)

    /** Cria uma matrícula vazia (usada ao ler do banco). */
    public Matricula() {
    }

    /** Cria uma matrícula com os dados principais. */
    public Matricula(String numeroMatricula, Integer idAluno, Integer idTurma, String status,
                     LocalDate dataMatricula, boolean andarilha, String motivoAndarilha) {
        this.numeroMatricula = numeroMatricula;
        this.idAluno = idAluno;
        this.idTurma = idTurma;
        this.status = status;
        this.dataMatricula = dataMatricula;
        this.andarilha = andarilha;
        this.motivoAndarilha = motivoAndarilha;
    }

    /** Devolve o identificador da matrícula. */
    public Integer getIdMatricula() {
        return idMatricula;
    }

    /** Define o identificador da matrícula. */
    public void setIdMatricula(Integer idMatricula) {
        this.idMatricula = idMatricula;
    }

    /** Devolve o número da matrícula. */
    public String getNumeroMatricula() {
        return numeroMatricula;
    }

    /** Define o número da matrícula. */
    public void setNumeroMatricula(String numeroMatricula) {
        this.numeroMatricula = numeroMatricula;
    }

    /** Devolve o identificador do aluno. */
    public Integer getIdAluno() {
        return idAluno;
    }

    /** Define o identificador do aluno. */
    public void setIdAluno(Integer idAluno) {
        this.idAluno = idAluno;
    }

    /** Devolve o identificador da turma. */
    public Integer getIdTurma() {
        return idTurma;
    }

    /** Define o identificador da turma. */
    public void setIdTurma(Integer idTurma) {
        this.idTurma = idTurma;
    }

    /** Devolve a situação. */
    public String getStatus() {
        return status;
    }

    /** Define a situação. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** Devolve a data da matrícula. */
    public LocalDate getDataMatricula() {
        return dataMatricula;
    }

    /** Define a data da matrícula. */
    public void setDataMatricula(LocalDate dataMatricula) {
        this.dataMatricula = dataMatricula;
    }

    /** Devolve a data de encerramento. */
    public LocalDate getDataEncerramento() {
        return dataEncerramento;
    }

    /** Define a data de encerramento. */
    public void setDataEncerramento(LocalDate dataEncerramento) {
        this.dataEncerramento = dataEncerramento;
    }

    /** Indica se a criança está no regime andarilha. */
    public boolean isAndarilha() {
        return andarilha;
    }

    /** Define se a criança está no regime andarilha. */
    public void setAndarilha(boolean andarilha) {
        this.andarilha = andarilha;
    }

    /** Devolve o motivo do regime andarilha. */
    public String getMotivoAndarilha() {
        return motivoAndarilha;
    }

    /** Define o motivo do regime andarilha. */
    public void setMotivoAndarilha(String motivoAndarilha) {
        this.motivoAndarilha = motivoAndarilha;
    }

    /** Devolve a data limite do regime andarilha. */
    public LocalDate getAndarilhaAte() {
        return andarilhaAte;
    }

    /** Define a data limite do regime andarilha. */
    public void setAndarilhaAte(LocalDate andarilhaAte) {
        this.andarilhaAte = andarilhaAte;
    }

    /** Devolve o nome da turma. */
    public String getNomeTurma() {
        return nomeTurma;
    }

    /** Define o nome da turma. */
    public void setNomeTurma(String nomeTurma) {
        this.nomeTurma = nomeTurma;
    }

    /** Indica se a turma está ativa. */
    public boolean isAtiva() {
        return ATIVA.equals(status);
    }

    /** Texto da matrícula nas listas: número e turma. */
    @Override
    public String toString() {
        return "Matrícula " + numeroMatricula + " (Status: " + status + ")";
    }
}