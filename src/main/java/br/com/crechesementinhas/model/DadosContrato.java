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

    /** Devolve o identificador da matrícula. */
    public int getIdMatricula() { return idMatricula; }
    /** Define o identificador da matrícula. */
    public void setIdMatricula(int idMatricula) { this.idMatricula = idMatricula; }

    /** Devolve o número da matrícula. */
    public String getNumeroMatricula() { return numeroMatricula; }
    /** Define o número da matrícula. */
    public void setNumeroMatricula(String numero) { this.numeroMatricula = numero; }

    /** Devolve a situação da matrícula. */
    public String getStatusMatricula() { return statusMatricula; }
    /** Define a situação da matrícula. */
    public void setStatusMatricula(String status) { this.statusMatricula = status; }

    /** Devolve o identificador do aluno. */
    public int getIdAluno() { return idAluno; }
    /** Define o identificador do aluno. */
    public void setIdAluno(int idAluno) { this.idAluno = idAluno; }

    /** Devolve o nome do aluno. */
    public String getNomeAluno() { return nomeAluno; }
    /** Define o nome do aluno. */
    public void setNomeAluno(String nomeAluno) { this.nomeAluno = nomeAluno; }

    /** Devolve a data de nascimento do aluno. */
    public LocalDate getDataNascimentoAluno() { return dataNascimentoAluno; }
    /** Define a data de nascimento do aluno. */
    public void setDataNascimentoAluno(LocalDate data) { this.dataNascimentoAluno = data; }

    /** Devolve o nome da turma. */
    public String getNomeTurma() { return nomeTurma; }
    /** Define o nome da turma. */
    public void setNomeTurma(String nomeTurma) { this.nomeTurma = nomeTurma; }

    /** Devolve o período da turma. */
    public String getPeriodoTurma() { return periodoTurma; }
    /** Define o período da turma. */
    public void setPeriodoTurma(String periodoTurma) { this.periodoTurma = periodoTurma; }

    /** Devolve os responsáveis. */
    public List<ResponsavelContrato> getResponsaveis() { return responsaveis; }
    /** Define os responsáveis. */
    public void setResponsaveis(List<ResponsavelContrato> responsaveis) { this.responsaveis = responsaveis; }


    /** O responsável indicado no vínculo como financeiro (quem paga), ou null se ninguém foi indicado. */
    public ResponsavelContrato getResponsavelFinanceiroIndicado() {
        for (ResponsavelContrato r : responsaveis) {
            if (r.isResponsavelFinanceiro()) {
                return r;
            }
        }
        return null;
    }

    /** Só os responsáveis LEGAIS (os únicos que podem assinar - RN-05). */
    public List<ResponsavelContrato> getResponsaveisLegais() {
        List<ResponsavelContrato> legais = new ArrayList<>();
        for (ResponsavelContrato r : responsaveis) {
            if (r.isResponsavelLegal()) {
                legais.add(r);
            }
        }
        return legais;
    }

    /** Procura um responsável pelo id (devolve null se não achar). */
    public ResponsavelContrato buscarResponsavel(int idResponsavel) {
        for (ResponsavelContrato r : responsaveis) {
            if (r.getIdResponsavel() == idResponsavel) {
                return r;
            }
        }
        return null;
    }

}