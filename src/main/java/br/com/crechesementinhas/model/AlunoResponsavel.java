package br.com.crechesementinhas.model;

import java.time.LocalDate;

/**
 * Vínculo entre uma criança e um responsável (RF-02, RN-01 a RN-03).
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

    /** Devolve o identificador do vínculo com o responsável. */
    public Integer getIdAlunoResponsavel() { return idAlunoResponsavel; }
    /** Define o identificador do vínculo com o responsável. */
    public void setIdAlunoResponsavel(Integer id) { this.idAlunoResponsavel = id; }
    /** Devolve o identificador do aluno. */
    public Integer getIdAluno() { return idAluno; }
    /** Define o identificador do aluno. */
    public void setIdAluno(Integer idAluno) { this.idAluno = idAluno; }
    /** Devolve o nome do aluno. */
    public String getNomeAluno() { return nomeAluno; }
    /** Define o nome do aluno. */
    public void setNomeAluno(String nomeAluno) { this.nomeAluno = nomeAluno; }
    /** Devolve o identificador do responsável. */
    public Integer getIdResponsavel() { return idResponsavel; }
    /** Define o identificador do responsável. */
    public void setIdResponsavel(Integer idResponsavel) { this.idResponsavel = idResponsavel; }
    /** Devolve o grau de parentesco. */
    public String getGrauParentesco() { return grauParentesco; }
    /** Define o grau de parentesco. */
    public void setGrauParentesco(String grauParentesco) { this.grauParentesco = grauParentesco; }
    /** Indica se a pessoa é responsável legal. */
    public boolean isResponsavelLegal() { return responsavelLegal; }
    /** Define se a pessoa é responsável legal. */
    public void setResponsavelLegal(boolean responsavelLegal) { this.responsavelLegal = responsavelLegal; }
    /** Indica se a pessoa é responsável financeiro. */
    public boolean isResponsavelFinanceiro() { return responsavelFinanceiro; }
    /** Define se a pessoa é responsável financeiro. */
    public void setResponsavelFinanceiro(boolean responsavelFinanceiro) { this.responsavelFinanceiro = responsavelFinanceiro; }
    /** Devolve o nome do responsável. */
    public String getNomeResponsavel() { return nomeResponsavel; }
    /** Define o nome do responsável. */
    public void setNomeResponsavel(String nomeResponsavel) { this.nomeResponsavel = nomeResponsavel; }
    /** Devolve o telefone do responsável. */
    public String getTelefoneResponsavel() { return telefoneResponsavel; }
    /** Define o telefone do responsável. */
    public void setTelefoneResponsavel(String telefoneResponsavel) { this.telefoneResponsavel = telefoneResponsavel; }
    /** Devolve a data de início. */
    public LocalDate getDataInicio() { return dataInicio; }
    /** Define a data de início. */
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    /** Devolve a data de fim. */
    public LocalDate getDataFim() { return dataFim; }
    /** Define a data de fim. */
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
}
