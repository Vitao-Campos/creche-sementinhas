package br.com.crechesementinhas.model;

import br.com.crechesementinhas.util.CalculadoraIdade;
import br.com.crechesementinhas.util.Cpf;
import java.time.LocalDate;

/**
 * Criança atendida pela creche (RF-02).
 * Espelha a tabela "aluno" do banco.
 */
public class Aluno {

    private Integer idAluno;
    private String nome;
    private LocalDate dataNascimento;
    private String cpf;          // só os 11 dígitos; null quando a criança não tem CPF
    private Sexo sexo;
    private String naturalidade;
    private String nacionalidade;
    private String observacao;

    /**
     * Idade completa em meses, calculada na hora (a faixa etária da turma usa isso, RN-07).
     */
    public long getIdadeEmMeses() {
        return CalculadoraIdade.emMeses(dataNascimento, LocalDate.now());
    }

    /** Idade escrita para as telas, como "2 anos e 1 mês". */
    public String getIdadeTexto() {
        return CalculadoraIdade.descrever(dataNascimento, LocalDate.now());
    }

    /** CPF com pontos e traço para exibir; vazio se a criança não tem CPF. */
    public String getCpfFormatado() {
        return cpf == null ? "" : Cpf.formatar(cpf);
    }

    /** Devolve o identificador do aluno. */
    public Integer getIdAluno() {
        return idAluno;
    }

    /** Define o identificador do aluno. */
    public void setIdAluno(Integer idAluno) {
        this.idAluno = idAluno;
    }

    /** Devolve o nome. */
    public String getNome() {
        return nome;
    }

    /** Define o nome. */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /** Devolve a data de nascimento. */
    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    /** Define a data de nascimento. */
    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    /** Devolve o CPF. */
    public String getCpf() {
        return cpf;
    }

    /** Define o CPF. */
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    /** Devolve o sexo. */
    public Sexo getSexo() {
        return sexo;
    }

    /** Define o sexo. */
    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    /** Devolve a naturalidade. */
    public String getNaturalidade() {
        return naturalidade;
    }

    /** Define a naturalidade. */
    public void setNaturalidade(String naturalidade) {
        this.naturalidade = naturalidade;
    }

    /** Devolve a nacionalidade. */
    public String getNacionalidade() {
        return nacionalidade;
    }

    /** Define a nacionalidade. */
    public void setNacionalidade(String nacionalidade) {
        this.nacionalidade = nacionalidade;
    }

    /** Devolve as observações. */
    public String getObservacao() {
        return observacao;
    }

    /** Define as observações. */
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    /** Texto da criança nas caixas de seleção: o nome. */
    @Override
    public String toString() {
        return "Aluno{id=" + idAluno + ", nome='" + nome + "'}";
    }
}
