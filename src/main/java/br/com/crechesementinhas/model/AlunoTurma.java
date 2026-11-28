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

    /** Diz se a criança tem matrícula ativa. */
    public boolean temMatriculaAtiva() {
        return idMatricula != null;
    }

    /** Calcula a idade da criança, em meses, na data de referência. */
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

    /** Devolve o texto da faixa etária da turma. */
    public String getFaixaTurmaTexto() {
        return temMatriculaAtiva() ? idadeMinimaMeses + " a " + idadeMaximaMeses : "";
    }

    /** Devolve o identificador do aluno. */
    public int getIdAluno() { return idAluno; }
    /** Define o identificador do aluno. */
    public void setIdAluno(int idAluno) { this.idAluno = idAluno; }

    /** Devolve o nome. */
    public String getNome() { return nome; }
    /** Define o nome. */
    public void setNome(String nome) { this.nome = nome; }

    /** Devolve a data de nascimento. */
    public LocalDate getDataNascimento() { return dataNascimento; }
    /** Define a data de nascimento. */
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    /** Devolve o identificador da matrícula. */
    public Integer getIdMatricula() { return idMatricula; }
    /** Define o identificador da matrícula. */
    public void setIdMatricula(Integer idMatricula) { this.idMatricula = idMatricula; }

    /** Devolve o número da matrícula. */
    public String getNumeroMatricula() { return numeroMatricula; }
    /** Define o número da matrícula. */
    public void setNumeroMatricula(String numeroMatricula) { this.numeroMatricula = numeroMatricula; }

    /** Indica se a criança está no regime andarilha. */
    public boolean isAndarilha() { return andarilha; }
    /** Define se a criança está no regime andarilha. */
    public void setAndarilha(boolean andarilha) { this.andarilha = andarilha; }

    /** Devolve o motivo do regime andarilha. */
    public String getMotivoAndarilha() { return motivoAndarilha; }
    /** Define o motivo do regime andarilha. */
    public void setMotivoAndarilha(String motivoAndarilha) { this.motivoAndarilha = motivoAndarilha; }

    /** Devolve a data limite do regime andarilha. */
    public LocalDate getAndarilhaAte() { return andarilhaAte; }
    /** Define a data limite do regime andarilha. */
    public void setAndarilhaAte(LocalDate andarilhaAte) { this.andarilhaAte = andarilhaAte; }

    /** Devolve o identificador da turma. */
    public Integer getIdTurma() { return idTurma; }
    /** Define o identificador da turma. */
    public void setIdTurma(Integer idTurma) { this.idTurma = idTurma; }

    /** Devolve o nome da turma. */
    public String getNomeTurma() { return nomeTurma; }
    /** Define o nome da turma. */
    public void setNomeTurma(String nomeTurma) { this.nomeTurma = nomeTurma; }

    /** Devolve a idade mínima, em meses. */
    public int getIdadeMinimaMeses() { return idadeMinimaMeses; }
    /** Define a idade mínima, em meses. */
    public void setIdadeMinimaMeses(int idadeMinimaMeses) { this.idadeMinimaMeses = idadeMinimaMeses; }

    /** Devolve a idade máxima, em meses. */
    public int getIdadeMaximaMeses() { return idadeMaximaMeses; }
    /** Define a idade máxima, em meses. */
    public void setIdadeMaximaMeses(int idadeMaximaMeses) { this.idadeMaximaMeses = idadeMaximaMeses; }
}
