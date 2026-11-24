package br.com.crechesementinhas.model;

import br.com.crechesementinhas.util.Telefone;

/**
 * Pessoa que pode buscar a criança (RF-02, RN-04). Espelha a tabela "pessoa_autorizada".
 * A mesma pessoa pode estar autorizada para várias crianças (irmãos).
 */
public class PessoaAutorizada {

    private Integer idPessoaAutorizada;
    private String nome;
    private String rg;
    private String telefone;   // só os dígitos

    public String getTelefoneFormatado() {
        return Telefone.formatar(telefone);
    }

    public Integer getIdPessoaAutorizada() { return idPessoaAutorizada; }
    public void setIdPessoaAutorizada(Integer id) { this.idPessoaAutorizada = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
}
