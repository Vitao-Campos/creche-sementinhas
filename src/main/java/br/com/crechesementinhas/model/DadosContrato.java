package br.com.crechesementinhas.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Tudo o que já está cadastrado e que o contrato reaproveita (RF-05):
 * dados da matrícula, do aluno, da turma e dos responsáveis.
 */
public class DadosContrato {

    private int idMatricula;
    private String numeroMatricula;
    private String statusMatricula;
    private int idAluno;
    private String nomeAluno;
    private LocalDate dataNascimentoAluno;
    private String nomeTurma;
    private String periodoTurma; // MANHA, TARDE ou INTEGRAL
    private List<ResponsavelContrato> responsaveis = new ArrayList<>();

    public int getIdMatricula() { return idMatricula; }
    public void setIdMatricula(int idMatricula) { this.idMatricula = idMatricula; }

    public String getNumeroMatricula() { return numeroMatricula; }
    public void setNumeroMatricula(String numero) { this.numeroMatricula = numero; }

    public String getStatusMatricula() { return statusMatricula; }
    public void setStatusMatricula(String status) { this.statusMatricula = status; }

    public int getIdAluno() { return idAluno; }
    public void setIdAluno(int idAluno) { this.idAluno = idAluno; }

    public String getNomeAluno() { return nomeAluno; }
    public void setNomeAluno(String nomeAluno) { this.nomeAluno = nomeAluno; }

    public LocalDate getDataNascimentoAluno() { return dataNascimentoAluno; }
    public void setDataNascimentoAluno(LocalDate data) { this.dataNascimentoAluno = data; }

    public String getNomeTurma() { return nomeTurma; }
    public void setNomeTurma(String nomeTurma) { this.nomeTurma = nomeTurma; }

    public String getPeriodoTurma() { return periodoTurma; }
    public void setPeriodoTurma(String periodoTurma) { this.periodoTurma = periodoTurma; }

    public List<ResponsavelContrato> getResponsaveis() { return responsaveis; }
    public void setResponsaveis(List<ResponsavelContrato> responsaveis) { this.responsaveis = responsaveis; }
}