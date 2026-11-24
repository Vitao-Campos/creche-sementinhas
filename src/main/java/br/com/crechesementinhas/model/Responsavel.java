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

    public String getCpfFormatado() {
        return cpf == null ? "" : Cpf.formatar(cpf);
    }

    public String getTelefoneFormatado() {
        return Telefone.formatar(telefone);
    }

    public Integer getIdResponsavel() { return idResponsavel; }
    public void setIdResponsavel(Integer idResponsavel) { this.idResponsavel = idResponsavel; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getProfissao() { return profissao; }
    public void setProfissao(String profissao) { this.profissao = profissao; }
}
