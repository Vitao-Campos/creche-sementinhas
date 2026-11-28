package br.com.crechesementinhas.model;

import br.com.crechesementinhas.util.Cpf;
import br.com.crechesementinhas.util.Telefone;

/**
 * Pai, mãe ou outro responsável pela criança (RF-02).
 * Espelha a tabela "responsavel" do banco.
 */
public class Responsavel {

    private Integer idResponsavel;
    private String nome;
    private String cpf;        // só os 11 dígitos
    private String telefone;   // só os dígitos (DDD + número)
    private String email;
    private String cep;        // só os 8 dígitos
    private String endereco;
    private String profissao;

    /** Devolve o CPF com máscara. */
    public String getCpfFormatado() {
        return cpf == null ? "" : Cpf.formatar(cpf);
    }

    /** Devolve o telefone com máscara. */
    public String getTelefoneFormatado() {
        return Telefone.formatar(telefone);
    }

    /** Devolve o identificador do responsável. */
    public Integer getIdResponsavel() { return idResponsavel; }
    /** Define o identificador do responsável. */
    public void setIdResponsavel(Integer idResponsavel) { this.idResponsavel = idResponsavel; }
    /** Devolve o nome. */
    public String getNome() { return nome; }
    /** Define o nome. */
    public void setNome(String nome) { this.nome = nome; }
    /** Devolve o CPF. */
    public String getCpf() { return cpf; }
    /** Define o CPF. */
    public void setCpf(String cpf) { this.cpf = cpf; }
    /** Devolve o telefone. */
    public String getTelefone() { return telefone; }
    /** Define o telefone. */
    public void setTelefone(String telefone) { this.telefone = telefone; }
    /** Devolve o e-mail. */
    public String getEmail() { return email; }
    /** Define o e-mail. */
    public void setEmail(String email) { this.email = email; }
    /** Devolve o CEP. */
    public String getCep() { return cep; }
    /** Define o CEP. */
    public void setCep(String cep) { this.cep = cep; }
    /** Devolve o endereço. */
    public String getEndereco() { return endereco; }
    /** Define o endereço. */
    public void setEndereco(String endereco) { this.endereco = endereco; }
    /** Devolve a profissão. */
    public String getProfissao() { return profissao; }
    /** Define a profissão. */
    public void setProfissao(String profissao) { this.profissao = profissao; }
}
