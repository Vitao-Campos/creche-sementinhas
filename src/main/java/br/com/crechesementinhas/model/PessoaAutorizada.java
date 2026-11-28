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

    /** Devolve o telefone com máscara. */
    public String getTelefoneFormatado() {
        return Telefone.formatar(telefone);
    }

    /** Devolve o identificador da pessoa autorizada. */
    public Integer getIdPessoaAutorizada() { return idPessoaAutorizada; }
    /** Define o identificador da pessoa autorizada. */
    public void setIdPessoaAutorizada(Integer id) { this.idPessoaAutorizada = id; }
    /** Devolve o nome. */
    public String getNome() { return nome; }
    /** Define o nome. */
    public void setNome(String nome) { this.nome = nome; }
    /** Devolve o RG. */
    public String getRg() { return rg; }
    /** Define o RG. */
    public void setRg(String rg) { this.rg = rg; }
    /** Devolve o telefone. */
    public String getTelefone() { return telefone; }
    /** Define o telefone. */
    public void setTelefone(String telefone) { this.telefone = telefone; }
}
