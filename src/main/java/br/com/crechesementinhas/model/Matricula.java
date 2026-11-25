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

    public Matricula() {
    }

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

    public Integer getIdMatricula() {
        return idMatricula;
    }

    public void setIdMatricula(Integer idMatricula) {
        this.idMatricula = idMatricula;
    }

    public String getNumeroMatricula() {
        return numeroMatricula;
    }

    public void setNumeroMatricula(String numeroMatricula) {
        this.numeroMatricula = numeroMatricula;
    }

    public Integer getIdAluno() {
        return idAluno;
    }

    public void setIdAluno(Integer idAluno) {
        this.idAluno = idAluno;
    }

    public Integer getIdTurma() {
        return idTurma;
    }

    public void setIdTurma(Integer idTurma) {
        this.idTurma = idTurma;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDataMatricula() {
        return dataMatricula;
    }

    public void setDataMatricula(LocalDate dataMatricula) {
        this.dataMatricula = dataMatricula;
    }

    public LocalDate getDataEncerramento() {
        return dataEncerramento;
    }

    public void setDataEncerramento(LocalDate dataEncerramento) {
        this.dataEncerramento = dataEncerramento;
    }

    public boolean isAndarilha() {
        return andarilha;
    }

    public void setAndarilha(boolean andarilha) {
        this.andarilha = andarilha;
    }

    public String getMotivoAndarilha() {
        return motivoAndarilha;
    }

    public void setMotivoAndarilha(String motivoAndarilha) {
        this.motivoAndarilha = motivoAndarilha;
    }

    public LocalDate getAndarilhaAte() {
        return andarilhaAte;
    }

    public void setAndarilhaAte(LocalDate andarilhaAte) {
        this.andarilhaAte = andarilhaAte;
    }

    public String getNomeTurma() {
        return nomeTurma;
    }

    public void setNomeTurma(String nomeTurma) {
        this.nomeTurma = nomeTurma;
    }

    public boolean isAtiva() {
        return ATIVA.equals(status);
    }

    @Override
    public String toString() {
        return "Matrícula " + numeroMatricula + " (Status: " + status + ")";
    }
}