package br.com.crechesementinhas.model;

import br.com.crechesementinhas.util.CalculadoraIdade;
import br.com.crechesementinhas.util.Cpf;
import java.time.LocalDate;

/**
 * Criança atendida pela creche (RF-03).
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
     * Idade completa em meses, calculada na hora (a faixa etária da turma usa isso, RN-01).
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

    public Integer getIdAluno() {
        return idAluno;
    }

    public void setIdAluno(Integer idAluno) {
        this.idAluno = idAluno;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    public String getNaturalidade() {
        return naturalidade;
    }

    public void setNaturalidade(String naturalidade) {
        this.naturalidade = naturalidade;
    }

    public String getNacionalidade() {
        return nacionalidade;
    }

    public void setNacionalidade(String nacionalidade) {
        this.nacionalidade = nacionalidade;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    @Override
    public String toString() {
        return "Aluno{id=" + idAluno + ", nome='" + nome + "'}";
    }
}
