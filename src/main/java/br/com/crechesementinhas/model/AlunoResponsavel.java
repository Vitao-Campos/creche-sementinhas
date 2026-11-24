package br.com.crechesementinhas.model;

import java.time.LocalDate;

/**
 * Vínculo entre uma criança e um responsável (RF-04).
 * Espelha a tabela "aluno_responsavel"; "nomeAluno" vem do JOIN, só para exibir.
 */
public class AlunoResponsavel {

    private Integer idAlunoResponsavel;
    private Integer idAluno;
    private String nomeAluno;
    private Integer idResponsavel;
    private String grauParentesco;
    private boolean responsavelLegal;
    private boolean responsavelFinanceiro;   // quem paga a mensalidade desta criança
    private String nomeResponsavel;          // vem do JOIN, só para exibir
    private String telefoneResponsavel;      // vem do JOIN, só para exibir
    private LocalDate dataInicio;
    private LocalDate dataFim;   // null = vínculo em vigor

    /** Vigente em "hoje": já começou e ainda não terminou. */
    public boolean estaVigente(LocalDate hoje) {
        return !dataInicio.isAfter(hoje) && (dataFim == null || !dataFim.isBefore(hoje));
    }

    public Integer getIdAlunoResponsavel() { return idAlunoResponsavel; }
    public void setIdAlunoResponsavel(Integer id) { this.idAlunoResponsavel = id; }
    public Integer getIdAluno() { return idAluno; }
    public void setIdAluno(Integer idAluno) { this.idAluno = idAluno; }
    public String getNomeAluno() { return nomeAluno; }
    public void setNomeAluno(String nomeAluno) { this.nomeAluno = nomeAluno; }
    public Integer getIdResponsavel() { return idResponsavel; }
    public void setIdResponsavel(Integer idResponsavel) { this.idResponsavel = idResponsavel; }
    public String getGrauParentesco() { return grauParentesco; }
    public void setGrauParentesco(String grauParentesco) { this.grauParentesco = grauParentesco; }
    public boolean isResponsavelLegal() { return responsavelLegal; }
    public void setResponsavelLegal(boolean responsavelLegal) { this.responsavelLegal = responsavelLegal; }
    public boolean isResponsavelFinanceiro() { return responsavelFinanceiro; }
    public void setResponsavelFinanceiro(boolean responsavelFinanceiro) { this.responsavelFinanceiro = responsavelFinanceiro; }
    public String getNomeResponsavel() { return nomeResponsavel; }
    public void setNomeResponsavel(String nomeResponsavel) { this.nomeResponsavel = nomeResponsavel; }
    public String getTelefoneResponsavel() { return telefoneResponsavel; }
    public void setTelefoneResponsavel(String telefoneResponsavel) { this.telefoneResponsavel = telefoneResponsavel; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
}
