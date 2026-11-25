package br.com.crechesementinhas.model;

import br.com.crechesementinhas.util.CalculadoraIdade;
import java.time.LocalDate;

/**
 * Uma linha da consulta de alunos por turma (Frente E): a criança, a matrícula ATIVA
 * (se houver) e a turma dessa matrícula. Vem de um SELECT com JOIN, não é uma tabela.
 */
public class AlunoTurma {

    private int idAluno;
    private String nome;
    private LocalDate dataNascimento;

    // Matrícula ATIVA (null quando a criança não está matriculada)
    private Integer idMatricula;
    private String numeroMatricula;
    private boolean andarilha;
    private String motivoAndarilha;
    private LocalDate andarilhaAte;

    // Turma da matrícula ativa
    private Integer idTurma;
    private String nomeTurma;
    private int idadeMinimaMeses;
    private int idadeMaximaMeses;

    public boolean temMatriculaAtiva() {
        return idMatricula != null;
    }

    public long idadeEmMeses(LocalDate referencia) {
        return CalculadoraIdade.emMeses(dataNascimento, referencia);
    }

    /**
     * Alerta de faixa etária (RF-07): a idade da criança hoje ainda cabe na turma dela?
     * A andarilha (RN-08) é a exceção: vale o prazo previsto, não a idade.
     */
    public SituacaoFaixaEtaria situacaoFaixa(LocalDate referencia) {
        if (!temMatriculaAtiva()) {
            return SituacaoFaixaEtaria.SEM_MATRICULA;
        }
        if (andarilha) {
            return SituacaoFaixaEtaria.deAndarilha(andarilhaAte, referencia);
        }
        return SituacaoFaixaEtaria.de(idadeEmMeses(referencia), idadeMinimaMeses, idadeMaximaMeses);
    }

    public String getFaixaTurmaTexto() {
        return temMatriculaAtiva() ? idadeMinimaMeses + " a " + idadeMaximaMeses : "";
    }

    public int getIdAluno() { return idAluno; }
    public void setIdAluno(int idAluno) { this.idAluno = idAluno; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public Integer getIdMatricula() { return idMatricula; }
    public void setIdMatricula(Integer idMatricula) { this.idMatricula = idMatricula; }

    public String getNumeroMatricula() { return numeroMatricula; }
    public void setNumeroMatricula(String numeroMatricula) { this.numeroMatricula = numeroMatricula; }

    public boolean isAndarilha() { return andarilha; }
    public void setAndarilha(boolean andarilha) { this.andarilha = andarilha; }

    public String getMotivoAndarilha() { return motivoAndarilha; }
    public void setMotivoAndarilha(String motivoAndarilha) { this.motivoAndarilha = motivoAndarilha; }

    public LocalDate getAndarilhaAte() { return andarilhaAte; }
    public void setAndarilhaAte(LocalDate andarilhaAte) { this.andarilhaAte = andarilhaAte; }

    public Integer getIdTurma() { return idTurma; }
    public void setIdTurma(Integer idTurma) { this.idTurma = idTurma; }

    public String getNomeTurma() { return nomeTurma; }
    public void setNomeTurma(String nomeTurma) { this.nomeTurma = nomeTurma; }

    public int getIdadeMinimaMeses() { return idadeMinimaMeses; }
    public void setIdadeMinimaMeses(int idadeMinimaMeses) { this.idadeMinimaMeses = idadeMinimaMeses; }

    public int getIdadeMaximaMeses() { return idadeMaximaMeses; }
    public void setIdadeMaximaMeses(int idadeMaximaMeses) { this.idadeMaximaMeses = idadeMaximaMeses; }
}
