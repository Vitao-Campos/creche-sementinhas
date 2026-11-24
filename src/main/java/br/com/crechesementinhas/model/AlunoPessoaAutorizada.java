package br.com.crechesementinhas.model;

import br.com.crechesementinhas.util.Telefone;

/**
 * Autorização de uma pessoa para uma criança (RF-02, RN-04).
 * Espelha "aluno_pessoa_autorizada"; nome, RG e telefone vêm do JOIN, só para exibir.
 */
public class AlunoPessoaAutorizada {

    private Integer idAlunoPessoaAutorizada;
    private Integer idAluno;
    private Integer idPessoaAutorizada;
    private String nome;
    private String rg;
    private String telefone;
    private String parentesco;
    private TipoAutorizacao tipoAutorizacao;
    private boolean ativo;

    public String getTelefoneFormatado() {
        return Telefone.formatar(telefone);
    }

    public Integer getIdAlunoPessoaAutorizada() { return idAlunoPessoaAutorizada; }
    public void setIdAlunoPessoaAutorizada(Integer id) { this.idAlunoPessoaAutorizada = id; }
    public Integer getIdAluno() { return idAluno; }
    public void setIdAluno(Integer idAluno) { this.idAluno = idAluno; }
    public Integer getIdPessoaAutorizada() { return idPessoaAutorizada; }
    public void setIdPessoaAutorizada(Integer id) { this.idPessoaAutorizada = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getParentesco() { return parentesco; }
    public void setParentesco(String parentesco) { this.parentesco = parentesco; }
    public TipoAutorizacao getTipoAutorizacao() { return tipoAutorizacao; }
    public void setTipoAutorizacao(TipoAutorizacao tipo) { this.tipoAutorizacao = tipo; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
